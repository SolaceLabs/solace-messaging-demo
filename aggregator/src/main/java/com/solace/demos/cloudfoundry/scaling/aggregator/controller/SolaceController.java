/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.solace.demos.cloudfoundry.scaling.aggregator.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.solace.demos.cloudfoundry.scaling.aggregator.model.GlobalStats;
import com.solace.demos.cloudfoundry.scaling.aggregator.model.JobRequest;
import com.solace.demos.cloudfoundry.scaling.aggregator.model.JobRequestStats;
import com.solace.demos.cloudfoundry.scaling.aggregator.model.JobSummary;
import com.solace.demos.cloudfoundry.scaling.aggregator.model.StatusSummary;
import com.solace.demos.cloudfoundry.scaling.aggregator.model.WorkInstance;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.SpringJCSMPFactory;

@Component
public class SolaceController {

    private static final Log trace = LogFactory.getLog(SolaceController.class);

    private JCSMPSession session;
    boolean isSolaceConnected = false;

    private final int NUM_PUBLISH_THREADS = 5;
    private ExecutorService executor;
    private Destination destForPublish;
    private Map<String, JobRequestStats> jobsTrackingMap = new HashMap<>();
    private GlobalStats globalStats = new GlobalStats();

    @Autowired
    private SpringJCSMPFactory solaceFactory;

    @PostConstruct
    public void init() {
        // Connect to Solace
        trace.info("************* Init Called ************");

        try {
            session = solaceFactory.createSession();
            session.connect();

            destForPublish = JCSMPFactory.onlyInstance().createQueue("Q/demo/requests");

            isSolaceConnected = true;
            System.out.println("************* Solace initialized correctly!! ************");

        } catch (Exception e) {
            trace.error("Error connecting and setting up session.", e);
        }

        executor = Executors.newFixedThreadPool(NUM_PUBLISH_THREADS);

    }

    public void startJobRequest(JobRequest jobRequest) throws Exception {

        // Check if job is already running.
        if (jobsTrackingMap.containsKey(jobRequest.getId())) {
            throw new Exception("Job ID already in use. Please select unique Job IDs.");
        }

        globalStats.incTotalJobs();

        List<WorkInstance> workQueue = new ArrayList<>();

        Random rn = new Random();
        for (long i = 0; i < jobRequest.getJobCount(); i++) {

            // Randomly generate work delay
            int workDelay = rn.nextInt(jobRequest.getMaxWorkDelayInMSec() - jobRequest.getMinWorkDelayInMSec() + 1)
                    + jobRequest.getMinWorkDelayInMSec();
            trace.warn("Work delay for Job: " + jobRequest.getId() + " . Item: " + i + " : Workdelay: " + workDelay);

            // Send time of 0 for now. Will be filled in on send.
            WorkInstance work = new WorkInstance(i, workDelay, 0, jobRequest.getId());
            workQueue.add(work);
        }

        JobRequestStats jobStatus = new JobRequestStats(jobRequest);
        jobsTrackingMap.put(jobRequest.getId(), jobStatus);

        // Start a worker thread with the new job.
        Runnable worker = new ProducerThread(jobRequest.getId(), workQueue, session, destForPublish,
                jobRequest.getRateInMsgsPerSec(), jobStatus, globalStats);
        executor.execute(worker);

    }

    public void deleteJobs() {
        // for now only delete jobs that are completed.
        for (Iterator<Map.Entry<String, JobRequestStats>> it = jobsTrackingMap.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, JobRequestStats> entry = it.next();
            if (entry.getValue().isJobComplete()) {
                it.remove();
            }
        }

    }

    @Override
    public synchronized String toString() {
        // A JSON Object containing full details of the stats.
        JSONObject jsonString = new JSONObject();

        List<String> jobsList = new ArrayList<>();
        for (JobRequestStats job : jobsTrackingMap.values()) {
            jobsList.add(job.toString());
        }

        jsonString.put("jobs", jobsList);

        return jsonString.toString();

    }

    public StatusSummary getStatus() {
        List<JobSummary> jobsList = new ArrayList<>();
        for (JobRequestStats job : jobsTrackingMap.values()) {

            jobsList.add(job.getSummary());
        }
        return new StatusSummary(jobsList, isSolaceConnected);
    }

    public GlobalStats getGlobalStats() {
        return globalStats;
    }

    public void resetGlobalStats() {
        globalStats.resetStats();
    }

}

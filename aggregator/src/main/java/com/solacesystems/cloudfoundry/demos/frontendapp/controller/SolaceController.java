package com.solacesystems.cloudfoundry.demos.frontendapp.controller;

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
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.stereotype.Component;

import com.solacesystems.cloudfoundry.demos.cloud.SolMessagingInfo;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.GlobalStats;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.JobRequest;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.JobRequestStats;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.JobSummary;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.StatusSummary;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.WorkInstance;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;

@Component
public class SolaceController {

	private static final Log trace = LogFactory.getLog(SolaceController.class);

	private JCSMPSession session;

	private final int NUM_PUBLISH_THREADS = 5;
	private ExecutorService executor;
	private Destination destForPublish;
	private Map<String, JobRequestStats> jobsTrackingMap = new HashMap<>();
	private GlobalStats globalStats = new GlobalStats();
	
	
	@PostConstruct
	public void init() {
		// Connect to Solace
		System.out.println("************* Init Called ************");
		CloudFactory cloudFactory = new CloudFactory();
		Cloud cloud = cloudFactory.getCloud();
		trace.error(cloud.getCloudProperties());
		SolMessagingInfo solmessaging = null;
		List<ServiceInfo> services = cloud.getServiceInfos();
		if (services == null) {
			trace.error("Null services");
		} else {
			for (ServiceInfo service : services) {
				trace.error(service);
				if (service instanceof SolMessagingInfo) {
					solmessaging = (SolMessagingInfo) service;
				}
			}
		}
		trace.error(System.getenv("VCAP_SERVICES"));

		if (solmessaging == null) {
			trace.error("Did not find Solace service. Aborting conenction");
			return;
		}

		System.out.println("BasicReplier initializing...");
		final JCSMPProperties properties = new JCSMPProperties();
		properties.setProperty(JCSMPProperties.HOST, solmessaging.getSmfUri());
		properties.setProperty(JCSMPProperties.VPN_NAME,
				solmessaging.getMsgVpn());
		properties.setProperty(JCSMPProperties.USERNAME,
				solmessaging.getUsername());
		properties.setProperty(JCSMPProperties.PASSWORD,
				solmessaging.getPassword());

		try {
			session = JCSMPFactory.onlyInstance().createSession(properties);
			session.connect();

			destForPublish = JCSMPFactory.onlyInstance().createQueue(
					"Q/demo/requests");

			System.out
					.println("************* Solace initialized correctly!! ************");

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
			int workDelay = rn.nextInt(jobRequest.getMaxWorkDelayInMSec()
					- jobRequest.getMinWorkDelayInMSec() + 1)
					+ jobRequest.getMinWorkDelayInMSec();
			trace.warn("Work delay for Job: " + jobRequest.getId()
					+ " . Item: " + i + " : Workdelay: " + workDelay);

			// Send time of 0 for now. Will be filled in on send.
			WorkInstance work = new WorkInstance(i, workDelay, 0,
					jobRequest.getId());
			workQueue.add(work);
		}

		JobRequestStats jobStatus = new JobRequestStats(jobRequest);
		jobsTrackingMap.put(jobRequest.getId(), jobStatus);

		// Start a worker thread with the new job.
		Runnable worker = new ProducerThread(jobRequest.getId(), workQueue,
				session, destForPublish, jobRequest.getRateInMsgsPerSec(),
				jobStatus, globalStats);
		executor.execute(worker);

	}
	
	public void deleteJobs() {
		// for now only delete jobs that are completed.
		for (Iterator<Map.Entry<String, JobRequestStats>> it = jobsTrackingMap
				.entrySet().iterator(); it.hasNext();) {
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
		return new StatusSummary(jobsList);
	}
	
	public GlobalStats getGlobalStats() {
		return globalStats;
	}
	
public void resetGlobalStats() {
		globalStats.resetStats();
	}

}

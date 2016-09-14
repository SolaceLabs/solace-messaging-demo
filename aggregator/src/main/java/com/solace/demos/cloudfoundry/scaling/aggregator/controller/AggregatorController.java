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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.solace.demos.cloudfoundry.scaling.aggregator.model.GlobalStats;
import com.solace.demos.cloudfoundry.scaling.aggregator.model.JobRequest;
import com.solace.demos.cloudfoundry.scaling.aggregator.model.StatusSummary;

@RestController
public class AggregatorController {

	Log log = LogFactory.getLog(AggregatorController.class);

	@Autowired
	SolaceController solaceController;
	
	@RequestMapping(value = "/jobs", method = RequestMethod.POST)
	public ResponseEntity<String> createJobs(@RequestBody JobRequest jobRequest) {

		log.warn("Entering createJobs");
		try {
			solaceController.startJobRequest(jobRequest);
		} catch (Exception e) {
			log.error("Service Creation failed.", e);
			return new ResponseEntity<>("{'description': '" + e.getMessage() + "'}", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("{}", HttpStatus.OK);
	}
	
	@RequestMapping(value = "/jobs", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteJobs() {
		
		log.warn("Entering deleteJobs");
		solaceController.deleteJobs();
		return new ResponseEntity<>("{}", HttpStatus.OK);
	}


	@RequestMapping("/status")
	public StatusSummary getStatus() {
		return solaceController.getStatus();
	}
	
	@RequestMapping("/stats/global")
	public GlobalStats getSolaceStatus() {
		return solaceController.getGlobalStats();
	}
	
	@RequestMapping(value = "/stats/global", method = RequestMethod.DELETE)
	public ResponseEntity<String> resetSolaceStatus() {
		log.warn("Entering deleteJobs");
		solaceController.resetGlobalStats();
		return new ResponseEntity<>("{}", HttpStatus.OK);
	}
}

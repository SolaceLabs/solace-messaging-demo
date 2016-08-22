package com.solacesystems.cloudfoundry.demos.frontendapp.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.solacesystems.cloudfoundry.demos.frontendapp.model.GlobalStats;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.JobRequest;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.StatusSummary;

@RestController
public class WorkerOffloadDemoController {

	Log log = LogFactory.getLog(WorkerOffloadDemoController.class);

	@Autowired
	SolaceController solaceController;

	@RequestMapping(value = "/v1/workerOffloadDemo/jobsForm", method = RequestMethod.POST)
	public ResponseEntity<String> createJobsForm() {

		log.warn("In JobsForm");
		
		return new ResponseEntity<>("{}", HttpStatus.OK);
	}
	
	@RequestMapping(value = "/v1/workerOffloadDemo/jobs", method = RequestMethod.POST)
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
	
	@RequestMapping(value = "/v1/workerOffloadDemo/jobs", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteJobs() {
		
		log.warn("Entering deleteJobs");
		solaceController.deleteJobs();
		return new ResponseEntity<>("{}", HttpStatus.OK);
	}


	@RequestMapping("/v1/workerOffloadDemo/stats/jobs")
	public StatusSummary jobs() {
		return solaceController.getStatus();
	}
	
	@RequestMapping("/v1/workerOffloadDemo/stats/global")
	public GlobalStats getSolaceStatus() {
		return solaceController.getGlobalStats();
	}
	
	@RequestMapping(value = "/v1/workerOffloadDemo/stats/global", method = RequestMethod.DELETE)
	public ResponseEntity<String> resetSolaceStatus() {
		log.warn("Entering deleteJobs");
		solaceController.resetGlobalStats();
		return new ResponseEntity<>("{}", HttpStatus.OK);
	}
}

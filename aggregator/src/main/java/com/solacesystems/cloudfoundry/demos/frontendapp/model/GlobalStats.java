package com.solacesystems.cloudfoundry.demos.frontendapp.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GlobalStats {

	@JsonSerialize
	private long totalJobs;
	
	@JsonSerialize
	private long finishedJobs;
	
	@JsonSerialize
	private long workRequestsSent;
	
	@JsonSerialize
	private long workResponsesRecv;
	
	
	public GlobalStats() {
	}

	public synchronized long getTotalJobs() {
		return totalJobs;
	}

	public synchronized long getFinishedJobs() {
		return finishedJobs;
	}

	public synchronized long getWorkRequestsSent() {
		return workRequestsSent;
	}
	
	public synchronized long getWorkResponsesRecv() {
		return workResponsesRecv;
	}
	
	public synchronized void resetStats() {
		totalJobs = 0;
		finishedJobs = 0;
		workRequestsSent = 0;
		workResponsesRecv = 0;
	}
	
	public synchronized void incTotalJobs() {
		totalJobs++;
	}
	
	public synchronized void incFinishedJobs() {
		finishedJobs++;
	}
	
	public synchronized void incWorkRequestSent() {
		workRequestsSent++;
	}
	
	public synchronized void incResponseReceived() {
		workResponsesRecv++;
	}
	
}

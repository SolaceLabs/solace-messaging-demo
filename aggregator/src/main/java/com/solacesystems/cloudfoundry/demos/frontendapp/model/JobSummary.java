package com.solacesystems.cloudfoundry.demos.frontendapp.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobSummary {

	@NotEmpty
	@JsonSerialize
	private final JobStatus jobStatus;
	
	@NotEmpty
	@JsonSerialize
	private final LatencySummary latency;
	
	@NotEmpty
	@JsonSerialize
	private final ResponsesSummary responses;
	
	@NotEmpty
	@JsonSerialize
	private final RequestsSummary requests;
	
	
	public JobSummary(JobStatus jobStatus, LatencySummary latency, ResponsesSummary responses, RequestsSummary requests) {
		this.jobStatus = jobStatus;
		this.latency = latency;
		this.responses = responses;
		this.requests = requests;
	}

	public JobStatus getJobStatus() {
		return jobStatus;
	}
	
	public LatencySummary getLatency() {
		return latency;
	}

	public ResponsesSummary getResponses() {
		return responses;
	}
	
	public RequestsSummary getRequests() {
		return requests;
	}
}

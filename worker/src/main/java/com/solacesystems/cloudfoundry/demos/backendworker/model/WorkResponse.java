package com.solacesystems.cloudfoundry.demos.backendworker.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkResponse {

	@NotEmpty
	@JsonSerialize
	@JsonProperty("id")
	private long id;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("workDelay")
	private int workDelay;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("sendTime")
	private long sendTime;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("clientName")
	private String clientName;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("jobId")
	private String jobId;
	
	public WorkResponse() {
	}

	public WorkResponse(long id, int workDelay, long sendTime, String clientName, String jobId) {
		this.id = id;
		this.workDelay = workDelay;
		this.sendTime = sendTime;
		this.clientName = clientName;
		this.jobId = jobId;
	}

	public long getId() {
		return id;
	}

	public int getWorkDelay() {
		return workDelay;
	}

	public long getSendTime() {
		return sendTime;
	}
	
	public String getClientName() {
		return clientName;
	}
	
	public String getJobId() {
		return jobId;
	}
}

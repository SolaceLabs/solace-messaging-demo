package com.solacesystems.cloudfoundry.demos.backendworker.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class WorkInstance {

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
	@JsonProperty("jobId")
	private String jobId;
	
	
	public WorkInstance() {
	}

	public WorkInstance(long id, int workDelay, long sendTime, String jobId) {
		this.id = id;
		this.workDelay = workDelay;
		this.sendTime = sendTime;
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
	
	public String getJobId() {
		return jobId;
	}
	
}

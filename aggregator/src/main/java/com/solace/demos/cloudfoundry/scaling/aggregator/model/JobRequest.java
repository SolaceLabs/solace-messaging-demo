package com.solace.demos.cloudfoundry.scaling.aggregator.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobRequest {

	@NotEmpty
	@JsonSerialize
	@JsonProperty("id")
	private String id;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("rateInMsgPerSec")
	private long rateInMsgPerSec;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("jobCount")
	private long jobCount;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("minWorkDelayInMSec")
	private int minWorkDelayInMSec;
	
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("maxWorkDelayInMSec")
	private int maxWorkDelayInMSec;
	
	
	public JobRequest() {
	}

	public JobRequest(String id, long rateInMsgPerSec, long jobCount, int minWorkDelayInMSec, int maxWorkDelayInMSec) {
		this.id = id;
		this.rateInMsgPerSec = rateInMsgPerSec;
		this.jobCount = jobCount;
		this.minWorkDelayInMSec = minWorkDelayInMSec;
		this.maxWorkDelayInMSec = maxWorkDelayInMSec;
	}

	public String getId() {
		return id;
	}

	public long getRateInMsgsPerSec() {
		return rateInMsgPerSec;
	}

	public long getJobCount() {
		return jobCount;
	}
	
	public int getMinWorkDelayInMSec() {
		return minWorkDelayInMSec;
	}
	
	public int getMaxWorkDelayInMSec() {
		return maxWorkDelayInMSec;
	}
}

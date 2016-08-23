package com.solace.demos.cloudfoundry.scaling.aggregator.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobStatus {

	@NotEmpty
	@JsonSerialize
	private final String jobId;
	
	@NotEmpty
	@JsonSerialize
	private final long workCount;
	
	@NotEmpty
	@JsonSerialize
	private final long rate;
	
	@NotEmpty
	@JsonSerialize
	private final long delayMin;
	
	@NotEmpty
	@JsonSerialize
	private final long delayMax;
	
	@NotEmpty
	@JsonSerialize
	private final boolean isComplete;

	public JobStatus(String jobId, long workCount, long rate, long delayMin,
			long delayMax, boolean isComplete) {
		this.jobId = jobId;
		this.workCount = workCount;
		this.rate = rate;
		this.delayMin = delayMin;
		this.delayMax = delayMax;
		this.isComplete = isComplete;
	}

	public String getJobId() {
		return jobId;
	}

	public long getWorkCount() {
		return workCount;
	}

	public long getRate() {
		return rate;
	}

	public long getDelayMin() {
		return delayMin;
	}

	public long getDelayMax() {
		return delayMax;
	}

	public boolean isComplete() {
		return isComplete;
	}
}

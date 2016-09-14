package com.solace.demos.cloudfoundry.scaling.aggregator.model;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusSummary {

    @JsonSerialize
    private final boolean isConnected;
    
	@NotEmpty
	@JsonSerialize
	private final List<JobSummary> jobs;
	
	public StatusSummary(List<JobSummary> jobs, boolean isConnected) {
		this.jobs = jobs;
		this.isConnected = isConnected;
	}

	public List<JobSummary> getJobs() {
		return jobs;
	}
	
	public boolean isConnected() {
	    return isConnected;
	}

}

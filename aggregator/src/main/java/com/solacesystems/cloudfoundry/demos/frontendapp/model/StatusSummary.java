package com.solacesystems.cloudfoundry.demos.frontendapp.model;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusSummary {

	@NotEmpty
	@JsonSerialize
	private final List<JobSummary> jobs;
	
	public StatusSummary(List<JobSummary> jobs) {
		this.jobs = jobs;
	}

	public List<JobSummary> getJobs() {
		return jobs;
	}

}

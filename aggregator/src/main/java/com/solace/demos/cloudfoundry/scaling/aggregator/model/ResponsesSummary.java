package com.solace.demos.cloudfoundry.scaling.aggregator.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponsesSummary {

	@NotEmpty
	@JsonSerialize
	private final long numResponseOk;
	
	@NotEmpty
	@JsonSerialize
	private final long numResponseErrors;
	
	@NotEmpty
	@JsonSerialize
	private final long numResponsesOutstanding;
	
	@NotEmpty
	@JsonSerialize
	private final double respPerSec;
	
	public ResponsesSummary(long numResponseOk, long numResponseErrors, long numResponsesOutstanding, double respPerSec) {
		this.numResponseOk = numResponseOk;
		this.numResponseErrors = numResponseErrors;
		this.numResponsesOutstanding = numResponsesOutstanding;
		this.respPerSec = respPerSec;
	}

	public long getNumResponseOk() {
		return numResponseOk;
	}

	public long getNumResponseErrors() {
		return numResponseErrors;
	}

	public long getNumResponseOutstanding() {
		return numResponsesOutstanding;
	}

	public double getRespPerSec() {
		return respPerSec;
	}
}

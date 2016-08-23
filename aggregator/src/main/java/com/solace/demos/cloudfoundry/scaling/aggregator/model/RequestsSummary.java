package com.solace.demos.cloudfoundry.scaling.aggregator.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestsSummary {

	@NotEmpty
	@JsonSerialize
	private final long numMsgsSent;
	
	@NotEmpty
	@JsonSerialize
	private final long numPubAcksOk;
	
	@NotEmpty
	@JsonSerialize
	private final long numPubAckErrors;
	
	@NotEmpty
	@JsonSerialize
	private final long numPubAckOutstanding;
	
	public RequestsSummary(long numMsgsSent, long numPubAcksOk, long numPubAckErrors, long numPubAckOutstanding) {
		this.numMsgsSent = numMsgsSent;
		this.numPubAcksOk = numPubAcksOk;
		this.numPubAckErrors = numPubAckErrors;
		this.numPubAckOutstanding = numPubAckOutstanding;
	}

	public long getNumMsgsSent() {
		return numMsgsSent;
	}

	public long getNumPubAcksOk() {
		return numPubAcksOk;
	}

	public long getNumPubAckErrors() {
		return numPubAckErrors;
	}

	public double getNumPubAckOutstanding() {
		return numPubAckOutstanding;
	}
}

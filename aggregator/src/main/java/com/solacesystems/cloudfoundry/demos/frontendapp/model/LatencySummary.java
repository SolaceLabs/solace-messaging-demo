package com.solacesystems.cloudfoundry.demos.frontendapp.model;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.NONE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LatencySummary {

	@NotEmpty
	@JsonSerialize
	private final long min;
	
	@NotEmpty
	@JsonSerialize
	private final long avg;
	
	@NotEmpty
	@JsonSerialize
	private final long max;
	
	@NotEmpty
	@JsonSerialize
	private final long perc50th;
	
	@NotEmpty
	@JsonSerialize
	private final long perc95th;
	
	@NotEmpty
	@JsonSerialize
	private final long perc99th;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("stdDev")
	private final long stdDev;
	
	@NotEmpty
	@JsonSerialize
	private final long bucketSize;
	
	@NotEmpty
	@JsonSerialize
	@JsonProperty("numResponsesCounted")
	private final long numResponses;

	public LatencySummary(long min, long avg, long max, long perc50th,
			long perc95th, long perc99th, long stdDev, long bucketSize,
			long numResponses) {
		this.min = min;
		this.avg = avg;
		this.max = max;
		this.perc50th = perc50th;
		this.perc95th = perc95th;
		this.perc99th = perc99th;
		this.stdDev = stdDev;
		this.bucketSize = bucketSize;
		this.numResponses = numResponses;
	}

	public long getMin() {
		return min;
	}

	public long getAvg() {
		return avg;
	}

	public long getMax() {
		return max;
	}

	public long get50thPerc() {
		return perc50th;
	}

	public long get95thPerc() {
		return perc95th;
	}

	public long get99thPerc() {
		return perc99th;
	}

	public long getStdDev() {
		return stdDev;
	}

	public long getBucketSize() {
		return bucketSize;
	}

	public long getNumResponses() {
		return numResponses;
	}
}

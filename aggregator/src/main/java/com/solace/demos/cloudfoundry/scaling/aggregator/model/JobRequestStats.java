package com.solace.demos.cloudfoundry.scaling.aggregator.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

public class JobRequestStats {

	public enum PerfStatType {
		NUM_MSGS_RECV,
		LATENCY_USEC_MIN,
		LATENCY_USEC_AVG,
		LATENCY_USEC_MAX,
		NUM_RESP_OK,
		NUM_RESP_ERRORS,
		NUM_LATENCY_MSGS,
		NUM_TPUT_MSGS,
		NUM_PUB_MSGS_SENT,
        NUM_PUB_MSGS_ACKED,
        NUM_PUB_MSGS_NACKED,
        NUM_PUB_ACK_ERRORS,
        SESSION_EVENT_UNKNOWN
	}

	private static final Log trace = LogFactory.getLog(JobRequestStats.class);
	
	private EnumMap<PerfStatType, MutableLong> _stats = 
		new EnumMap<PerfStatType, MutableLong>(PerfStatType.class);
	long _receiveStartTimeInNanos, _receiveEndTimeInNanos, _totalLatency;
	double _toUs;
	
	private List<MutableLong> _latencyBuckets;
	private int _latencyGranularity;
	private double _latencyWarmupInSecs;
	Set<Long> expectedAcksSet;
	Set<Long> expectedResponseSet;
	private JobRequest jobRequest;
	
	public JobRequestStats(JobRequest jobRequest) {
		// Pick some reasonable defaults for our use case.
	    // This means buckets are 131 msec in size for a max latency of around 4.5 min.  
		this(jobRequest, 2048, 15, 0);
	}
	
	public JobRequestStats(
			JobRequest jobRequest, 
		final int latencyNumBuckets,
		final int latencyGranularityFactor,
		final double latencyWarmUpSecs) {

		this.jobRequest = jobRequest;
		
		for (PerfStatType s : PerfStatType.values()) {
			_stats.put(s, new MutableLong(0));
		}

		_latencyBuckets = new ArrayList<MutableLong>(latencyNumBuckets);
		for (int i = 0; i < latencyNumBuckets; i++) {
			_latencyBuckets.add(new MutableLong(0));
		}
		
		_latencyGranularity = latencyGranularityFactor;
		_latencyWarmupInSecs = latencyWarmUpSecs;
		_toUs = 1000; // nano to micro.
		
		_totalLatency = 0;
		this.resetStats();
	}

	public synchronized void requestBeingSent(WorkInstance workInstance) {
		
		
		_stats.get(PerfStatType.NUM_PUB_MSGS_SENT).increment();
		
		expectedAcksSet.add(workInstance.getId());
		expectedResponseSet.add(workInstance.getId());
		if (trace.isDebugEnabled())
			trace.debug("JobId: " + jobRequest.getId() + " Adding work id: " + workInstance.getId());
	}

	public synchronized void pubAckReceived(WorkInstance workInstance, boolean sentSuccessfully) {
		
		// Should be expecting this Ack
		if (expectedAcksSet.contains(workInstance.getId())) {
			if (sentSuccessfully) {
				_stats.get(PerfStatType.NUM_PUB_MSGS_ACKED).increment();
			} else {
				_stats.get(PerfStatType.NUM_PUB_MSGS_NACKED).increment();
			}
			expectedAcksSet.remove(workInstance.getId());
		} else {
			trace.warn("Did not expect ack for this request: " + workInstance);
			_stats.get(PerfStatType.NUM_PUB_ACK_ERRORS).increment();
		}
	}
	
	public synchronized void newResponseReceived(WorkResponse workResponse, long timeRecvd) {
		
		_stats.get(PerfStatType.NUM_MSGS_RECV).increment();
		long latency = timeRecvd - workResponse.getSendTime();
		
		if (latency < 0) {
			trace.warn("Negative latency. Sent: " + timeRecvd + " Received: " + workResponse.getSendTime() + " Latency: " + latency);
			latency = 0;
		} 
		
		incLatency(timeRecvd, latency);
		
		// Should be expecting this Ack
		if (expectedResponseSet.contains(workResponse.getId())) {
			_stats.get(PerfStatType.NUM_RESP_OK).increment();
			expectedResponseSet.remove(workResponse.getId());
		} else {
			trace.warn("Did not expect this response: " + workResponse);
			_stats.get(PerfStatType.NUM_RESP_ERRORS).increment();
		}
	}
	
	public synchronized boolean isJobComplete() {
		
		// If job is still in progress return false.
		if (jobRequest.getJobCount() != this.getStat(PerfStatType.NUM_PUB_MSGS_SENT)) {
			return false;
		}
		
		// If done sending then wait for all replies.
		if (expectedAcksSet.isEmpty() && expectedResponseSet.isEmpty()) {
			return true;
		} else { 
			return false;
		}
	}

	public synchronized void resetStats() {
		for (MutableLong ml : _stats.values()) {
			ml.setValue(0);
		}
		_receiveEndTimeInNanos = 0;
		_receiveStartTimeInNanos = 0;
		_totalLatency = 0;

		for (MutableLong bucket : _latencyBuckets) {
			bucket.setValue(0);
		}
		_stats.get(PerfStatType.LATENCY_USEC_MIN).setValue(Long.MAX_VALUE);
		_stats.get(PerfStatType.LATENCY_USEC_MAX).setValue(0);
		
		expectedAcksSet = new HashSet<>();
		expectedResponseSet = new HashSet<>();
	}

	

	
	@Override
	public synchronized String toString() {
		// A JSON Object containing full details of the stats.
		JSONObject statusJson = new JSONObject();
		
		JSONObject requestTrackingJson = new JSONObject();
		requestTrackingJson.put("workSent", this.getStat(PerfStatType.NUM_PUB_MSGS_SENT));
		requestTrackingJson.put("pubAckOutstanding", expectedAcksSet.size());
		requestTrackingJson.put("numPubAcks", this.getStat(PerfStatType.NUM_PUB_MSGS_ACKED));
		requestTrackingJson.put("numPubNack", this.getStat(PerfStatType.NUM_PUB_MSGS_NACKED));
		requestTrackingJson.put("numPubAckErrors", this.getStat(PerfStatType.NUM_PUB_ACK_ERRORS));
		statusJson.put("requests", requestTrackingJson);
				
		JSONObject responseTrackingJson = new JSONObject();
		responseTrackingJson.put("numResponseErrors", this.getStat(PerfStatType.NUM_RESP_ERRORS));
		responseTrackingJson.put("numResponseOk", this.getStat(PerfStatType.NUM_RESP_OK));
		responseTrackingJson.put("responsesOutstanding", expectedResponseSet.size());
		statusJson.put("responses", responseTrackingJson);
		
		JSONObject latencyJson = new JSONObject();
		latencyJson.put("bucketSize", this.getLatencyBucketSize() );
		latencyJson.put("numResponsesCounted", this.getStat(PerfStatType.NUM_LATENCY_MSGS));
		latencyJson.put("min", this.getStat(PerfStatType.LATENCY_USEC_MIN));
		latencyJson.put("avg", this.getStat(PerfStatType.LATENCY_USEC_AVG));
		latencyJson.put("50th", this.getPercentileLatencyInUSec(50));
		latencyJson.put("95th", this.getPercentileLatencyInUSec(95));
		latencyJson.put("99th", this.getPercentileLatencyInUSec(99));
		latencyJson.put("max", this.getStat(PerfStatType.LATENCY_USEC_MAX));
		latencyJson.put("deviation", this.getLatencyStdDevInUSec());
		statusJson.put("latency", latencyJson);
		
		JSONObject ratesJson = new JSONObject();
		ratesJson.put("respPerSec", this.getThruPut(PerfStatType.NUM_TPUT_MSGS));
		statusJson.put("rates", ratesJson);
		
		statusJson.put("jobId", jobRequest.getId());
		return statusJson.toString();
	}
	
	public JobSummary getSummary() {
		
		// Summary
		JobStatus jobStatus = new JobStatus(
				jobRequest.getId(),
				jobRequest.getJobCount(),
				jobRequest.getRateInMsgsPerSec(),
				jobRequest.getMinWorkDelayInMSec(),
				jobRequest.getMaxWorkDelayInMSec(),
				this.isJobComplete());
		
		// Requests
		RequestsSummary reqSum = new RequestsSummary(
				this.getStat(PerfStatType.NUM_PUB_MSGS_SENT),
				this.getStat(PerfStatType.NUM_PUB_MSGS_ACKED),
				this.getStat(PerfStatType.NUM_PUB_MSGS_NACKED) + this.getStat(PerfStatType.NUM_PUB_ACK_ERRORS),
				expectedAcksSet.size());
		
		// Responses
		ResponsesSummary respSum = new ResponsesSummary(
				this.getStat(PerfStatType.NUM_RESP_OK),
				this.getStat(PerfStatType.NUM_RESP_ERRORS),
				expectedResponseSet.size(),
				this.getThruPut(PerfStatType.NUM_TPUT_MSGS));
		
		// Latency
		LatencySummary latSum = new LatencySummary(
				this.getStat(PerfStatType.LATENCY_USEC_MIN),
				this.getStat(PerfStatType.LATENCY_USEC_AVG),
				this.getStat(PerfStatType.LATENCY_USEC_MAX),
				this.getPercentileLatencyInUSec(50),
				this.getPercentileLatencyInUSec(95),
				this.getPercentileLatencyInUSec(99),
				this.getLatencyStdDevInUSec(),
				this.getLatencyBucketSize(),
				this.getStat(PerfStatType.NUM_LATENCY_MSGS));
		
		return new JobSummary(jobStatus, latSum, respSum, reqSum);
	}
	
	private synchronized void updateTimeRecvd(long timeRecvd) {
		_receiveEndTimeInNanos = timeRecvd;
		_receiveStartTimeInNanos = (_receiveStartTimeInNanos == 0) ? timeRecvd : _receiveStartTimeInNanos;
		// Must transfer num messages to throughput messages when endtime is updated.
        _stats.get(PerfStatType.NUM_TPUT_MSGS).setValue(
		_stats.get(PerfStatType.NUM_MSGS_RECV).longValue());
	}
	
	private synchronized void incLatency(long timeRecvd, long latency) {
		updateTimeRecvd(timeRecvd);

	    // If we're in the warmup period then skip this stat.
	    if ((double)timeRecvd < ((double)_receiveStartTimeInNanos + 
	                              _latencyWarmupInSecs * 1000000000.0)) {
	        return;
	    }

	    
	    // Convert latency to usec.
	    long usecLatency = (long)((double)latency / _toUs);
	    
	    MutableLong minLat = _stats.get(PerfStatType.LATENCY_USEC_MIN);
	    if(minLat.longValue() > usecLatency) { 
	    	minLat.setValue(usecLatency);
	    }
	    
	    MutableLong maxLat = _stats.get(PerfStatType.LATENCY_USEC_MAX);
	    if(maxLat.longValue() < usecLatency) { 
	    	maxLat.setValue(usecLatency);
	    }
	    
	    _totalLatency += usecLatency;
	    
	    _stats.get(PerfStatType.NUM_LATENCY_MSGS).increment();
	    
	    // Update the buckets
	    long bucketIndex = usecLatency >> _latencyGranularity;
	    if (bucketIndex >= _latencyBuckets.size()) {
	        bucketIndex = _latencyBuckets.size() - 1;
	    }
	    
	    if (bucketIndex < 0) {
	    	trace.error("Negative latency: " + usecLatency + " BucketIndex: " + bucketIndex + " _latencyBuckets: " + _latencyBuckets.size());
	    	bucketIndex = 0;
	    }
	    
	    _latencyBuckets.get((int) bucketIndex).increment() ;
	}
	
	private synchronized double getThruPut(PerfStatType type) {
		double result;
		
		long startTime = _receiveStartTimeInNanos;
		if (type == PerfStatType.NUM_LATENCY_MSGS) {
			startTime = _receiveStartTimeInNanos + (long)(_latencyWarmupInSecs * 1000000000.0);
		}
		if (startTime >= _receiveEndTimeInNanos) {
			result = 0;
		} else {
			result = ((double)_stats.get(type).longValue() * 1000000000.0 / 
					 (double)(_receiveEndTimeInNanos - startTime));
		}
		return result;
	}

	private synchronized long getStat(PerfStatType type) {
		long val = 0;
		switch (type) {
		case LATENCY_USEC_AVG:
			if (_stats.get(PerfStatType.NUM_LATENCY_MSGS).longValue() != 0) {
				val = _totalLatency / _stats.get(PerfStatType.NUM_LATENCY_MSGS).longValue();
			} else {
				// Leave value as 0;
			}
			break;
		case LATENCY_USEC_MIN:
			if (_stats.get(PerfStatType.LATENCY_USEC_MIN).longValue() == Long.MAX_VALUE) {
				val = 0;
			} else {
				val = _stats.get(PerfStatType.LATENCY_USEC_MIN).longValue();
			}
			break;
		default:
			val = _stats.get(type).longValue();
		}

		return val;
	}
	
	private synchronized long getPercentileLatencyInUSec( double perc )
	{
	    long percentNumSamples = (long)
	    	(_stats.get(PerfStatType.NUM_LATENCY_MSGS).longValue() * perc / 100);
	    long numSamples = 0;
	    double latencyUSec = 0.0;
	    boolean isDone = false;
	    double bucketSizeUSec = ((double)(((long)1) << _latencyGranularity));
	    double bucketEndUSec = bucketSizeUSec;
	    
	    for (int bucketLoop = 0;
	         bucketLoop < _latencyBuckets.size() &&
	         isDone != true;
	         bucketLoop++) {
	        if (_latencyBuckets.get(bucketLoop).longValue() > 0) {
	            numSamples += _latencyBuckets.get(bucketLoop).longValue();
	            if (numSamples >= percentNumSamples &&
	                bucketLoop != (_latencyBuckets.size() - 1)) {
	                isDone = true;
	                latencyUSec = bucketEndUSec;
	            }
	        }
	        bucketEndUSec += bucketSizeUSec;
	    }

	    return (long) latencyUSec;
	}
	
	private long getLatencyStdDevInUSec()
	{
	    long numSamples = _stats.get(PerfStatType.NUM_LATENCY_MSGS).longValue();
	    double stdDevSum = 0.0;
	    double bucketSizeUSec = ((double)(((long)1) << _latencyGranularity));
	    double bucketEndUSec = bucketSizeUSec;
	    double avgLatency = getStat(PerfStatType.LATENCY_USEC_AVG);
	    double stdDev = 0;
	    
	    for (int bucketLoop = 0;
	         bucketLoop < _latencyBuckets.size();
	         bucketLoop++) {
	        if (_latencyBuckets.get(bucketLoop).longValue() > 0) {
	        	double tempSum = (bucketEndUSec - avgLatency) * (bucketEndUSec - avgLatency);
	        	stdDevSum += tempSum * _latencyBuckets.get(bucketLoop).longValue();
	        }
	        bucketEndUSec += bucketSizeUSec;
	    }

	    stdDev = Math.sqrt(stdDevSum / numSamples);
	    return (long) stdDev;
	}
	
	private long getLatencyBucketSize() {
		return ((((long)1) << _latencyGranularity));
	}
	
}
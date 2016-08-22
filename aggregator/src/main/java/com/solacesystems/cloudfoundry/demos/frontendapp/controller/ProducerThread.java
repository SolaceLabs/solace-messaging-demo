package com.solacesystems.cloudfoundry.demos.frontendapp.controller;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.GlobalStats;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.JobRequestStats;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.WorkInstance;
import com.solacesystems.cloudfoundry.demos.frontendapp.model.WorkResponse;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.Producer;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.SDTException;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class ProducerThread implements Runnable {

	Log log = LogFactory.getLog(WorkerOffloadDemoController.class);

	String jobId;
	private List<WorkInstance> workQueue;
	long msgRate;
	JCSMPSession session;
	Producer producer;
	private long numMsgsToPublish;
	Destination destForPublish;
	private final String MSG_TYPE_HEADER = "MSG_TYPE";
	private final String REQUEST_MSG_TYPE = "DEMO_REQUEST";
	private final String RESPONSE_MSG_TYPE = "DEMO_RESPONSE";
	JobRequestStats jobStatus;
	GlobalStats globalStats;

	class PublisherEventHandler implements
			JCSMPStreamingPublishCorrelatingEventHandler {

		@Override
		public void handleErrorEx(Object key, JCSMPException cause,
				long timestamp) {
			if (key instanceof WorkInstance) {
				WorkInstance workInstance = (WorkInstance) key;
				jobStatus.pubAckReceived(workInstance, false);
			} else {
				log.error("Received unexpected correlation in handleErrorEx: "
						+ key);
			}
		}

		@Override
		public void responseReceivedEx(Object key) {
			if (key instanceof WorkInstance) {
				WorkInstance workInstance = (WorkInstance) key;
				jobStatus.pubAckReceived(workInstance, true);
			} else {
				log.error("Received unexpected correlation in responseReceivedEx: "
						+ key);
			}
		}

		@Override
		public void handleError(String messageID, JCSMPException cause,
				long timestamp) {
			// Never called
		}

		@Override
		public void responseReceived(String messageID) {
			// Never called
		}

	};

	class DemoMessageListener implements XMLMessageListener {

		@Override
		public void onReceive(BytesXMLMessage response) {

			// Check if application will understand the message. Look for a
			// message type property with value "DEMO_RESPONSE"
			SDTMap propertiesMap = response.getProperties();
			if (propertiesMap == null
					|| !propertiesMap.containsKey(MSG_TYPE_HEADER)
					|| !(response instanceof TextMessage)) {
				log.warn("Receive message not matching expected format.");
				log.warn(response.dump());
				response.ackMessage();
				return;
			}

			String msgType = null;
			try {
				msgType = propertiesMap.getString(MSG_TYPE_HEADER);
			} catch (SDTException e1) {
				log.warn("Error retrieving message header.", e1);
				log.warn(response.dump());
				response.ackMessage();
				return;
			}

			if (!msgType.equals(RESPONSE_MSG_TYPE)) {
				log.warn("Message Type not expected. Received: " + msgType);
				log.warn(response.dump());
				response.ackMessage();
				return;
			}

			try {
				WorkResponse workResponse = (WorkResponse) new ObjectMapper()
						.readValue(((TextMessage) response).getText(),
								WorkResponse.class);
				long timeRecvd = System.nanoTime();
				jobStatus.newResponseReceived(workResponse, timeRecvd);
				// Track the response globally too.
				globalStats.incResponseReceived();
			} catch (IOException e) {
				log.error("Unable to parse message body.", e);
				log.error(response.dump());
			}

			
			response.ackMessage();
		}

		@Override
		public void onException(JCSMPException e) {
			System.out.printf("Consumer received exception: %s%n", e);
		}

	}

	public ProducerThread(String jobId, List<WorkInstance> workQueue,
			JCSMPSession session, Destination destForPublish, long msgRate,
			JobRequestStats jobStatus, GlobalStats globalStats) {
		this.jobId = jobId;
		this.workQueue = workQueue;
		this.session = session;
		this.destForPublish = destForPublish;
		this.msgRate = msgRate;
		this.jobStatus = jobStatus;
		this.globalStats = globalStats;
	}

	@Override
	public void run() {

		long interDocDelayNs;
		numMsgsToPublish = workQueue.size();

		if (msgRate == 0) {
			// 0 is a magic number, it means "as fast as you can go"
			interDocDelayNs = 0;
		} else {
			interDocDelayNs = (long) (1000000000 / msgRate);
		}

		try {
			long cntPublished = 0;

			if (log.isDebugEnabled()) {
				log.debug(String.format("JOB Request " + jobId
						+ ":About to enter publish loop (%s) messages",
						numMsgsToPublish));
			}

			// Need a temporary endpoint to receive back all the responses. For
			// this, we'll choose a temporary endpoint
			// so we can tolerate connectivity blibs without loss.

			Queue replyQueue = session.createTemporaryQueue();

			ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
			flowProps.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
			flowProps.setEndpoint(replyQueue);
			FlowReceiver flow = session.createFlow(new DemoMessageListener(),
					flowProps);
			flow.start();

			// Get a producer:
			XMLMessageProducer producer = session
					.getMessageProducer(new PublisherEventHandler());

			SDTMap solaceMsgProperties = JCSMPFactory.onlyInstance()
					.createMap();
			solaceMsgProperties.putString(MSG_TYPE_HEADER, REQUEST_MSG_TYPE);
			TextMessage cachedSolaceMessage = JCSMPFactory.onlyInstance()
					.createMessage(TextMessage.class);
			cachedSolaceMessage.setDeliveryMode(DeliveryMode.PERSISTENT);
			cachedSolaceMessage.setProperties(solaceMsgProperties);

			cachedSolaceMessage.setReplyTo(replyQueue);
			
			long startTimeInCycles = System.nanoTime();
			ObjectMapper jsonMapper = new ObjectMapper();
			
			for (WorkInstance workInstance : workQueue) {

				docPubDelay(startTimeInCycles, interDocDelayNs, cntPublished);

				// Update send time on each send.
				workInstance.setSendTime(System.nanoTime());
				
				// Populate message contents with JSON.
				cachedSolaceMessage.setText(jsonMapper.writeValueAsString(workInstance));

				System.out.println("*** About to send message: "+ jsonMapper.writeValueAsString(workInstance));
				
				// Allow ack correlation through the workInstance;
				cachedSolaceMessage.setCorrelationKey(workInstance);

				jobStatus.requestBeingSent(workInstance);

				producer.send(cachedSolaceMessage, destForPublish);

				cntPublished++;
				
				globalStats.incWorkRequestSent();

			}

			if (log.isDebugEnabled()) {
				log.debug(String.format("JOB Request " + jobId
						+ ":Exited publish loop (numpublished=%s)",
						cntPublished));
			}

			// Wait for all acks and responses to be processed.
			int guard = 100;
			while (!jobStatus.isJobComplete() && guard > 0) {
				Thread.sleep(500);
				guard--;
			}
			
			// For global stats, declare the job done either way at this point.
			globalStats.incFinishedJobs();

			if (!jobStatus.isJobComplete()) {
				log.error("JOB Request " + jobId
						+ ": Not all responses received correctly.");
				log.debug("JOB Request " + jobId + ": Job Status: "
						+ jobStatus.toString());

			} else {
				if (log.isDebugEnabled()) {
					log.debug(String.format("JOB Request " + jobId
							+ ":All responses received correctly"));
				}

			}
			
			// Clean up temp flow.
			flow.close();
		} catch (Exception e) {
			log.error(
					"JOB Request " + jobId
							+ ": Error during send. Aborting job due to: "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Performs the inter-doc delay. If operating at low rate, delay is applied
	 * between each doc. If operating at high rate (> 500 msgs/sec), delay is
	 * applied only once per 10 docs.
	 * 
	 * @param cntPublished
	 *            Count of messages published
	 */
	private final long docPubDelay(final long pubStartTimeNs,
			final long interDocDelayNs, final long cntPublished) {
		long timeWaited = 0;
		long nextPubTimeNs;
		if (interDocDelayNs > 0) {

			long timeSinceStartInNs = interDocDelayNs * cntPublished;
			/*
			 * The pause runs only every RATE_CHK_INTERV docs.
			 */
			nextPubTimeNs = pubStartTimeNs + timeSinceStartInNs;
			timeWaited = waitUntil(nextPubTimeNs);
		}
		return timeWaited;
	}

	private static final long waitUntil(long targetNanoTime) {
		long curtime = System.nanoTime();
		long waittimeInMs = (targetNanoTime - curtime) / 1000000;
		if (waittimeInMs > 0) {
			try {
				Thread.sleep(waittimeInMs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return waittimeInMs;
	}
}

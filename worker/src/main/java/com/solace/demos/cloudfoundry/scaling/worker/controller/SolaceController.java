/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.solace.demos.cloudfoundry.scaling.worker.controller;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.demos.cloudfoundry.scaling.worker.model.WorkInstance;
import com.solace.demos.cloudfoundry.scaling.worker.model.WorkResponse;
import com.solacesystems.common.util.trace.Trace;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.SDTException;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.SpringJCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

@Component
public class SolaceController {

	private static final Log trace = LogFactory.getLog(SolaceController.class);

	
	private final String MSG_TYPE_HEADER = "MSG_TYPE";
	private final String REQUEST_MSG_TYPE = "DEMO_REQUEST";
	private final String RESPONSE_MSG_TYPE = "DEMO_RESPONSE";
	
	@Autowired
    private SpringJCSMPFactory solaceFactory;

    private XMLMessageProducer producer;
	private JCSMPSession session;
	private String clientName; 
	
	class PublisherEventHandler implements JCSMPStreamingPublishEventHandler {
		@Override
		public void responseReceived(String messageID) {
			trace.debug("Producer received response for msg: "
					+ messageID);
		}

		@Override
		public void handleError(String messageID, JCSMPException e,
				long timestamp) {
			trace.error("Producer received error for msg: "+ messageID + " - " + timestamp, e);
		}

	};
	
	class DemoMessageListener implements XMLMessageListener {
		
		@Override
		public void onReceive(BytesXMLMessage request) {

			// Check if application will understand the message. Look for a message type property with value "DEMO_REQUEST"
			SDTMap propertiesMap = request.getProperties();
			if (propertiesMap == null ||
				!propertiesMap.containsKey(MSG_TYPE_HEADER) ||
				request.getReplyTo() == null ||
				!(request instanceof TextMessage) ) {
				trace.warn("Receive message not matching expected format.");
				trace.warn(request.dump());
				request.ackMessage();
				return;
			}
			
			String msgType = null;
			try {
				msgType = propertiesMap.getString(MSG_TYPE_HEADER);
			} catch (SDTException e1) {
				trace.warn("Error retrieving message header.", e1);
				trace.warn(request.dump());
				request.ackMessage();
				return;
			}
			
			if (!msgType.equals(REQUEST_MSG_TYPE)) {
				trace.warn("Message Type not expected. Received: " + msgType);
				trace.warn(request.dump());
				request.ackMessage();
				return;
			}
			
			try {
				ObjectMapper jsonMapper = new ObjectMapper();
				WorkInstance workInstance = (WorkInstance) jsonMapper.readValue(((TextMessage) request).getText(),
								WorkInstance.class);
				
				// Search in the message body for the "work load" and simulate work by sleeping
				// for that time.
				
				int delayInMSec = workInstance.getWorkDelay();
				
				// Do "work"
				Thread.sleep(delayInMSec);

				// Build response
				WorkResponse workResponse = new WorkResponse(workInstance.getId(), delayInMSec, workInstance.getSendTime(), clientName, workInstance.getJobId());
				
				TextMessage reply = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);

				
				
				SDTMap solaceMsgProperties = JCSMPFactory.onlyInstance().createMap();
				solaceMsgProperties.putString(MSG_TYPE_HEADER, RESPONSE_MSG_TYPE);
				reply.setProperties(solaceMsgProperties);
				reply.setText(jsonMapper.writeValueAsString(workResponse));
				reply.setDeliveryMode(DeliveryMode.PERSISTENT);
				
				trace.debug("*** About to send response: "+ jsonMapper.writeValueAsString(workResponse));
				
				producer.sendReply(request, reply);
				
			} catch (IOException e) {
				trace.error("Unable to parse message body.", e);
				trace.error(request.dump());
			} catch (InterruptedException e) {
				trace.error("Message work sleep interupted", e);
			} catch (JCSMPException e) {
				trace.error("Unable to send response.", e);
			} 
			
			request.ackMessage();
		}

		@Override
		public void onException(JCSMPException e) {
			System.out.printf(
					"Consumer received exception: %s%n", e);
		}

		
	}

	@PostConstruct
	public void init() {
		// Connect to Solace
		trace.info("************* Init Called ************");
		
		try {
		    session = solaceFactory.createSession();
            session.connect();
			
			clientName = (String)session.getProperty(JCSMPProperties.CLIENT_NAME);

			final Queue queue = JCSMPFactory.onlyInstance().createQueue("Q/demo/requests");
			 
			// set queue permissions to "consume" and access-type to "exclusive"
			final EndpointProperties endpointProps = new EndpointProperties();
			endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
			endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
			 
			// Actually provision it, and do not fail if it already exists
			session.provision(queue, endpointProps, JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
			
			producer = session.getMessageProducer(new PublisherEventHandler());

			final ConsumerFlowProperties flowProp = new ConsumerFlowProperties();
			flowProp.setEndpoint(queue);
			flowProp.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
			
			final FlowReceiver cons = session.createFlow(new DemoMessageListener(), flowProp, endpointProps);
			cons.start();
			
			System.out.println("************* Solace initialized correctly!! ************");
			

		} catch (Exception e) {
			Trace.error("Error connecting and setting up session.", e);
		}
	}

}

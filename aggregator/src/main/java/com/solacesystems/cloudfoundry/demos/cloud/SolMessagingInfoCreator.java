package com.solacesystems.cloudfoundry.demos.cloud;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.cloudfoundry.CloudFoundryServiceInfoCreator;
import org.springframework.cloud.cloudfoundry.Tags;

public class SolMessagingInfoCreator extends CloudFoundryServiceInfoCreator<SolMessagingInfo> {

	private static final Log trace = LogFactory
			.getLog(SolMessagingInfoCreator.class);
	
	public SolMessagingInfoCreator() {
		super(new Tags("solmessaging"));
	}

	@SuppressWarnings("unchecked")
	public SolMessagingInfo createServiceInfo(Map<String,Object> serviceData) {
		String id = getId(serviceData);

		// Not doing any null checking for now in demo.
		
		Map<String, Object> credentials = getCredentials(serviceData);

		Map<String, Object> authorization = (Map<String, Object>) credentials.get("authorization");
		
		Map<String, Object> basicAuthorization = (Map<String, Object>) authorization.get("basic");
		
		String username = getStringFromMap(basicAuthorization, "user", "username");
		String password = getStringFromMap(basicAuthorization, "pass", "password");
		
		Map<String, Object> connectivity = (Map<String, Object>) credentials.get("connectivity");
		Map<String, Object> smfService = (Map<String, Object>) connectivity.get("smf");
		
		String messageVpn = getStringFromMap(smfService, "msgVpn");
		String smfUri = getStringFromMap(smfService, "smfUri");
		String smfsUri = getStringFromMap(smfService, "smfTlsUri");
		String smfZipUri = getStringFromMap(smfService, "smfZipUri");
		
		SolMessagingInfo newConnectivity = new SolMessagingInfo(id, username, password, messageVpn, smfUri, smfsUri, smfZipUri);
		
		// Also set the other services, null if not found.
		newConnectivity.configurWebMessagingService(
				getStringFromMap(smfService, "smfWebUri"),
				getStringFromMap(smfService, "smfWebTlsUri"));

		Map<String, Object> restService = (Map<String, Object>) connectivity.get("rest");
		newConnectivity.configurRestService(
				getStringFromMap(restService, "httpUri"),
				getStringFromMap(restService, "httpsUri"));
		
		Map<String, Object> jmsService = (Map<String, Object>) connectivity.get("jms");
		newConnectivity.configurJmsService(
				getStringFromMap(jmsService, "jmsUri"),
				getStringFromMap(jmsService, "jmsTlsUri"));
		
		Map<String, Object> mqttService = (Map<String, Object>) connectivity.get("mqtt");
		newConnectivity.configurMqttService(
				getStringFromMap(mqttService, "mqttUri"),
				getStringFromMap(mqttService, "mqttTlsUri"),
				getStringFromMap(mqttService, "mqttWsUri"),
				getStringFromMap(mqttService, "mqttWssUri"));
		
		
		
		return newConnectivity;
	}
	

	protected String getStringFromMap(Map<String, Object> incomingMap, String... keys) {
		if (incomingMap == null) {
			trace.error("null map looking for " + keys);
			return null;
		}
		
		for (String key : keys) {
			if (incomingMap.containsKey(key)) {
				return (String) incomingMap.get(key);
			}
		}
		return null;
	}
	
	// These will come later from future version of Spring Cloud. For now clone methods here.
	
	protected String getId(Map<String, Object> serviceData) {
		return (String) serviceData.get("name");
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> getCredentials(Map<String, Object> serviceData) {
		return (Map<String, Object>) serviceData.get("credentials");
	}
}

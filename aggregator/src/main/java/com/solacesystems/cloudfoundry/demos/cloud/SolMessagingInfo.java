package com.solacesystems.cloudfoundry.demos.cloud;

import org.json.JSONObject;
import org.springframework.cloud.service.BaseServiceInfo;
import org.springframework.cloud.service.ServiceInfo.ServiceLabel;

/*
 * As a start this is just a simple demo of how you can get access to all of the Solace services.
 * In general the Solace message router can fulfill various service roles and could be modeled at 
 * completely separate services.
 */
@ServiceLabel("solmessaging")
public class SolMessagingInfo extends BaseServiceInfo {

	private String username;
	private String password;
	private String msgVpn;

	// For now don't model these as services. Simply store the string for demo
	// purposes.
	private String smfUri;
	private String smfsUri;
	private String smfZipUri;
	private String smfWsUri;
	private String smfWssUri;
	private String restHttpUri;
	private String restHttpsUri;
	private String jmsUri;
	private String jmsTlsUri;
	private String mqttUri;
	private String mqttTlsUri;
	private String mqttWsUri;
	private String mqttWssUri;

	public SolMessagingInfo(String id) {
		super(id);
	}

	
	public SolMessagingInfo(String id, String username, String password,
			String messageVpn, String smfUri, String smfsUri, String smfZipUri) {
		super(id);
		this.username = username;
		this.password = password;
		this.msgVpn = messageVpn;
		this.smfUri = smfUri;
		this.smfsUri = smfsUri;
		this.smfZipUri = smfZipUri;
	}
	
	public void configurWebMessagingService(String smfWsUri, String smfWssUri) {
		this.smfWsUri = smfWsUri;
		this.smfWssUri = smfWssUri;
	}
	
	public void configurRestService(String restHttpUri, String restHttpsUri) {
		this.restHttpUri = restHttpUri;
		this.restHttpsUri = restHttpsUri;
	}

	public void configurJmsService(String jmsUri, String jmsTlsUri) {
		this.jmsUri = jmsUri;
		this.jmsTlsUri = jmsTlsUri;
	}

	public void configurMqttService(String mqttUri, String mqttTlsUri, String mqttWsUri, String mqttWssUri) {
		this.mqttUri = mqttUri;
		this.mqttTlsUri = mqttTlsUri;
		this.mqttWsUri = mqttWsUri;
		this.mqttWssUri = mqttWssUri;
		
	}
	

	@ServiceProperty(category = "connection")
	public String getUsername() {
		return username;
	}

	@ServiceProperty(category = "connection")
	public String getPassword() {
		return password;
	}
	
	@ServiceProperty(category = "connection")
	public String getMsgVpn() {
		return msgVpn;
	}
	
	@ServiceProperty(category = "connection")
	public String getSmfUri() {
		return smfUri;
	}
	
	@ServiceProperty(category = "connection")
	public String getSmfZipUri() {
		return smfZipUri;
	}
	
	@ServiceProperty(category = "connection")
	public String getSmfsUri() {
		return smfsUri;
	}
	
	@ServiceProperty(category = "connection")
	public Boolean isEnabledForWebMessaging() {
		return (smfWsUri != null || smfWssUri != null);
	}

	@ServiceProperty(category = "connection")
	public String getSmfWebUri() {
		return smfWsUri;
	}
	
	@ServiceProperty(category = "connection")
	public String getSmfWebTlsUri() {
		return smfWssUri;
	}
	
	@ServiceProperty(category = "connection")
	public Boolean isEnabledForRestMessaging() {
		return (restHttpUri != null || restHttpsUri != null);
	}
	
	@ServiceProperty(category = "connection")
	public String getRestHttpUri() {
		return restHttpUri;
	}
	
	@ServiceProperty(category = "connection")
	public String getRestHttpsUri() {
		return restHttpsUri;
	}
		
	@ServiceProperty(category = "connection")
	public Boolean isEnabledForJmsMessaging() {
		return (jmsUri != null || jmsTlsUri != null);
	}
	
	@ServiceProperty(category = "connection")
	public String getJmsUri() {
		return jmsUri;
	}
	
	@ServiceProperty(category = "connection")
	public String getJmsTlsUri() {
		return jmsTlsUri;
	}
	
	@ServiceProperty(category = "connection")
	public Boolean isEnabledForMqttMessaging() {
		return (mqttUri != null || mqttTlsUri != null || mqttWsUri != null || mqttWssUri != null);
	}
	
	@ServiceProperty(category = "connection")
	public String getMqttUri() {
		return mqttUri;
	}
	
	@ServiceProperty(category = "connection")
	public String getMqttTlsUri() {
		return mqttTlsUri;
	}
	
	@ServiceProperty(category = "connection")
	public String getMqttWsUri() {
		return mqttWsUri;
	}
	
	@ServiceProperty(category = "connection")
	public String getMqttWssUri() {
		return mqttWssUri;
	}
	
	@Override
	public String toString() {
		JSONObject retJson = new JSONObject();
		
		retJson.put("serviceId", id);
		
		retJson.put("username", username);
		retJson.put("password", password);
		retJson.put("msgVpn", msgVpn);
		retJson.put("smfUri", smfUri);
		retJson.put("smfsUri", smfsUri);
		retJson.put("smfZipUri", smfZipUri);
		
		retJson.put("isWeb", isEnabledForWebMessaging());
		retJson.put("smfWebUri", smfWsUri);
		retJson.put("smfWebTlsUri", smfWssUri);
		
		retJson.put("isRest", isEnabledForRestMessaging());
		retJson.put("restHttpUri", restHttpUri);
		retJson.put("restHttpsUri", restHttpsUri);
		
		retJson.put("isJms", isEnabledForJmsMessaging());
		retJson.put("jmsUri", jmsUri);
		retJson.put("jmsTlsUri", jmsTlsUri);
		
		retJson.put("isMqtt", isEnabledForMqttMessaging());
		retJson.put("mqttUri", mqttUri);
		retJson.put("mqttTlsUri", mqttTlsUri);
		retJson.put("mqttWsUri", mqttWsUri);
		retJson.put("mqttWssUri", mqttWssUri);
		
		return retJson.toString(4);
	}
	
}
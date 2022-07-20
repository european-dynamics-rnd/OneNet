package it.eng.idsa.dataapp.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("application.ecc")
public class ECCProperties {
	private String protocol;
	private String RESTprotocol;
	private String host;
	private int port;
	private int RESTport;
	private String mixContext;
	private String formContext;
	private String headerContext;
	
	private String brokerRegisterContext;
	private String brokerUpdateContext;
	private String brokerDeleteContext;
	private String brokerPassivateContext;
	private String brokerQuerryContext;
	
	public String getProtocol() {
		return protocol;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getRESTprotocol() {
		return RESTprotocol;
	}
	public void setRESTprotocol(String rESTprotocol) {
		RESTprotocol = rESTprotocol;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getRESTport() {
		return RESTport;
	}
	public void setRESTport(int RESTport) {
		this.RESTport = RESTport;
	}
	public String getMixContext() {
		return mixContext;
	}
	public void setMixContext(String mixContext) {
		this.mixContext = mixContext;
	}
	public String getFormContext() {
		return formContext;
	}
	public void setFormContext(String formContext) {
		this.formContext = formContext;
	}
	public String getHeaderContext() {
		return headerContext;
	}
	public void setHeaderContext(String headerContext) {
		this.headerContext = headerContext;
	}
	public String getBrokerRegisterContext() {
		return brokerRegisterContext;
	}
	public void setBrokerRegisterContext(String brokerRegisterContext) {
		this.brokerRegisterContext = brokerRegisterContext;
	}
	public String getBrokerUpdateContext() {
		return brokerUpdateContext;
	}
	public void setBrokerUpdateContext(String brokerUpdateContext) {
		this.brokerUpdateContext = brokerUpdateContext;
	}
	public String getBrokerDeleteContext() {
		return brokerDeleteContext;
	}
	public void setBrokerDeleteContext(String brokerDeleteContext) {
		this.brokerDeleteContext = brokerDeleteContext;
	}
	public String getBrokerPassivateContext() {
		return brokerPassivateContext;
	}
	public void setBrokerPassivateContext(String brokerPassivateContext) {
		this.brokerPassivateContext = brokerPassivateContext;
	}
	public String getBrokerQuerryContext() {
		return brokerQuerryContext;
	}
	public void setBrokerQuerryContext(String brokerQuerryContext) {
		this.brokerQuerryContext = brokerQuerryContext;
	}
	
}

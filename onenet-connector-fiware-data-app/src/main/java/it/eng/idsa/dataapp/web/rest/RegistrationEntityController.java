package it.eng.idsa.dataapp.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.service.ProxyService;

@RestController

public class RegistrationEntityController {

	private static final Logger logger = LoggerFactory.getLogger(RegistrationEntityController.class);
	
	private String myBody = null;
	
	@Autowired
	private ProxyService proxyService;
	
	@Value("${application.fiware.ecc.provider.url}") 
	private String providerURL;

	@Value("${application.fiware.contextBroker.provider.url}") 
	private String contextBrokerProviderUrl;
	
	@Value("${application.fiware.contextpath.orionregistration}") 
	private String contextPathOrionRegistration;
	
	@Value("${application.fiware.contextpath.registration}") 
	private String contextPathRegistrationEntity;
	
	@Value("${application.orion.protocol}")
	private String orionProtocol;
	
	@Value("${application.orion.host}")
	private String orionHost;

	@Value("${application.orion.port.registration}")
	private String orionPort;

	@RequestMapping("/registration/**")
	public ResponseEntity<?> proxyToDataApp(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = true) Map<String, String> userData, HttpMethod method, HttpServletRequest request) throws URISyntaxException, Exception {
		logger.info("HTTP Method {}", method.name());
		logger.info("HTTP Headers {}", httpHeaders.entrySet().stream().map(Map.Entry::toString).collect(Collectors.joining(";", "[", "]")));
		
		logger.info("ENG: request.getRequestURI()       {}", request.getRequestURI());
		logger.info("ENG: contextPathOrionRegistration  {}", contextPathOrionRegistration);
		logger.info("ENG: contextPathRegistrationEntity {}", contextPathRegistrationEntity);
		
		//Replace contextPathGetEntity with contextPathOrionProvider
		String myAppo = new String (request.getRequestURI());
		myAppo = myAppo.replace(contextPathRegistrationEntity, contextPathOrionRegistration);
		StringBuilder contextPath = new StringBuilder(myAppo);
		logger.info("ENG: Path {}", contextPath.toString());
		if(StringUtils.isNotBlank(request.getQueryString())) {
			contextPath.append("?")
				.append(request.getQueryString());
		}
		logger.info("ENG: httpHeaders                {}", httpHeaders);
		logger.info("ENG: method                     {}", method);
		logger.info("ENG: request                    {}", request);
		logger.info("ENG: userData.get(\"entityId\") {}", userData.get("entityId"));
		logger.info("ENG: userData.get(\"eccUrl\")   {}", userData.get("eccUrl"));
		logger.info("ENG: userData.get(\"brokerUrl\"){}", userData.get("brokerUrl"));
		
//TODO RICOMINCIARE DA QUI!!!
//TODO cambiare entity forward-to e provider-context-broker prenderlo dal body
		String strEntity = "";
		strEntity = userData.get("entityId");
		if (strEntity.isEmpty())
			logger.error ("entity IS EMPTY");
		
		String strForwardTo = "";
		strForwardTo = userData.get("eccUrl");
		if (strForwardTo.isEmpty()) {
			logger.error ("entityId IS EMPTY");
		}

		String strPCB = "";
		strPCB = userData.get("brokerUrl");
		if (strPCB.isEmpty())
			logger.error ("provider-context-broker IS EMPTY");
		
		logger.info ("entity                  {}", strEntity);
		logger.info ("forward-to              {}", strForwardTo);
		logger.info ("provider-context-broker {}", strPCB);
		logger.info ("httpHeaders             {}", httpHeaders);
		logger.info ("contextPath             {}", contextPath);

		myBody = "{\r\n"
				+ "    \"multipart\": \"form\",\r\n"
				+ "    \"Forward-To\": \"https://192.168.1.85:8889/data\",\r\n"
				+ "    \"messageType\": \"ArtifactRequestMessage\",\r\n"
				+ "    \"requestedArtifact\": \"http://w3id.org/engrd/connector/artifact/1\",\r\n"
				+ "    \"payload\": {\r\n"
				+ "            \"@context\": [\r\n"
				+ "                \"https://fiware.github.io/data-models/context.jsonld\",\r\n"
				+ "                \"https://uri.etsi.org/ngsi-ld/v1/ngsi-ld-core-context-v1.3.jsonld\"\r\n"
				+ "            ],\r\n"
				+ "            \"type\": \"ContextSourceRegistration\",\r\n"
				+ "            \"information\": [{\r\n"
				+ "                    \"entities\": [{\r\n"
				+ "                            \"type\": \"Building\",\r\n"
				+ "                            \"id\":\"" + strEntity + "\"\r\n"
				+ "                        }\r\n"
				+ "                    ]\r\n"
				+ "                }\r\n"
				+ "            ],\r\n"
				+ "            \"endpoint\": \"http://" + strForwardTo + "\","
				+ "            \"provider-context-broker\": \"http://" + strPCB + "\""
				+ "    }\r\n"
				+ "}";
		
		
		logger.info("ENG: ************************************************************");
		logger.info("ENG: myBody {}", myBody);
		logger.info("ENG: ************************************************************");
		
		String body = null;
		body = myBody;

		logger.info("ENG: contextBrokerProviderUrl {}", contextBrokerProviderUrl);
		logger.info("ENG: ecc.provider.url         {}", providerURL);
		
		httpHeaders.add("entity", strEntity);
		httpHeaders.add("Forward-To", strForwardTo);
		httpHeaders.add("Provider-Context-Broker", strPCB);
		httpHeaders.add("Content-Length", String.valueOf(body.length()));
		httpHeaders.add("Content-Type", "application/ld+json");
		
		ProxyRequest proxyRequest = proxyService.parseIncommingProxyRequest(body);
		
		logger.info("ENG: proxyRequest.getPayload() {}", proxyRequest.getPayload());

		logger.info("Forwarding request using {}", ProxyRequest.MULTIPART_MIXED);
		
		
		URI myUri =  new URI (orionProtocol, null, orionHost, Integer.valueOf(orionPort), "/" + contextPathOrionRegistration + "/", null, null);

		return proxyService.proxyRegistrationEntityForm(proxyRequest, httpHeaders, contextPath.toString(), myUri);
		
	}
}

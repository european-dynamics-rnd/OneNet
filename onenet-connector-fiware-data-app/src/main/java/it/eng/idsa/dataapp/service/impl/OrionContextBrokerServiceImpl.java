package it.eng.idsa.dataapp.service.impl;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import it.eng.idsa.dataapp.model.ContextBrokerException;
import it.eng.idsa.dataapp.model.OrionRequest;
import it.eng.idsa.dataapp.service.OrionContextBrokerService;

@Service
public class OrionContextBrokerServiceImpl implements OrionContextBrokerService {
	
	private static final Logger logger = LoggerFactory.getLogger(OrionContextBrokerServiceImpl.class);

	private RestTemplate restTemplate;
	private String providerContextBrokerURL;
	
	public OrionContextBrokerServiceImpl(RestTemplate restTemplate,
			@Value("${application.fiware.contextBroker.provider.url}") String providerContextBrokerURL) {
		this.restTemplate = restTemplate;
		this.providerContextBrokerURL = providerContextBrokerURL;
	}
	
	@Override
	public ResponseEntity<String> enitityCall(OrionRequest orionRequest) {
		URI orionURI = URI.create(providerContextBrokerURL + orionRequest.getRequestPath());
		logger.info("Triggering request towards {}", orionURI);

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> request;
		
		logger.info("Executing {} request", orionRequest.getMethod());
		headers = orionRequest.getHeaders();
		headers.remove("Forward-To");
		request = new HttpEntity<String>(orionRequest.getOriginalPayload(), headers);
		
		ResponseEntity<String> response;
		try {
			logger.info(" ENG orionRequest.getHeaders()         {}", orionRequest.getHeaders());
			logger.info(" ENG orionRequest.getMethod()          {}", orionRequest.getMethod());
			logger.info(" ENG orionRequest.getOriginalPayload() {}", orionRequest.getOriginalPayload());
			logger.info(" ENG orionRequest.getRequestPath()     {}", orionRequest.getRequestPath());
			logger.info(" ENG orionRequest.getStatusCode()      {}", orionRequest.getStatusCode());

			logger.info(" ENG orionURI                          {}", orionURI);
			logger.info(" ENG request                           {}", request);
			response = restTemplate.exchange(orionURI, orionRequest.getMethod(), request, String.class);
		}catch (HttpStatusCodeException e) {
			throw new ContextBrokerException(e, orionRequest.getMethod());
		}
		logger.info("Response received {}\n with status code {}", response.getBody(), response.getStatusCode());
		return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
	}

}

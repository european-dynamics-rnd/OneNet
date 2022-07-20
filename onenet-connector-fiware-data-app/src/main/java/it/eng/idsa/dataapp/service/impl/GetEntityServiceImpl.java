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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.idsa.dataapp.model.GetEntityException;
import it.eng.idsa.dataapp.model.GetEntityRequest;
import it.eng.idsa.dataapp.model.OrionRequest;
import it.eng.idsa.dataapp.service.GetEntityService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Service
public class GetEntityServiceImpl implements GetEntityService {

	private static final Logger logger = LoggerFactory.getLogger(GetEntityServiceImpl.class);

	private RestTemplate restTemplate;
	private String providerContextBrokerURL;
	
	public GetEntityServiceImpl(RestTemplate restTemplate,
			@Value("${application.fiware.contextBroker.provider.url}") String providerContextBrokerURL) {
		this.restTemplate = restTemplate;
		this.providerContextBrokerURL = providerContextBrokerURL;
	}
	
	@Override
	public ResponseEntity<String> entityCall(GetEntityRequest getEntityRequest) {
		URI orionURI = URI.create(providerContextBrokerURL + getEntityRequest.getRequestPath());
		logger.info("Triggering request towards {}", orionURI);

		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> request;
		
		logger.info("Executing {} request", getEntityRequest.getMethod());
		headers = getEntityRequest.getHeaders();
		
		request = new HttpEntity<String>(getEntityRequest.getOriginalPayload(), headers);
		
		ResponseEntity<String> response;
		try {
			logger.info(" ENG orionRequest.getHeaders()         {}", getEntityRequest.getHeaders());
			logger.info(" ENG orionRequest.getMethod()          {}", getEntityRequest.getMethod());
			logger.info(" ENG orionRequest.getOriginalPayload() {}", getEntityRequest.getOriginalPayload());
			logger.info(" ENG orionRequest.getRequestPath()     {}", getEntityRequest.getRequestPath());
			logger.info(" ENG orionRequest.getStatusCode()      {}", getEntityRequest.getStatusCode());

			logger.info(" ENG orionURI                          {}", orionURI);
			logger.info(" ENG request                           {}", request);
			response = restTemplate.exchange(orionURI, getEntityRequest.getMethod(), request, String.class);
		}catch (HttpStatusCodeException e) {
			throw new GetEntityException(e, getEntityRequest.getMethod());
		}
		logger.info("Response received {}\n with status code {}", response.getBody(), response.getStatusCode());
		return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
	}

	@Override
	public ResponseEntity<String> convertToDataAppResponse(ResponseEntity<String> resultEntity) {
		
		logger.info("ENG: Start...");

		logger.info("ENG: resultEntity.getHeaders <{}>", resultEntity.getHeaders());
		logger.info("ENG: resultEntity.getBody    <{}>", resultEntity.getBody());

		ObjectMapper mapper = new ObjectMapper();
		OrionRequest orionRequest = null;
		MultipartMessage mMessage = MultipartMessageProcessor.parseMultipartMessage(resultEntity.getBody());
		
		try {
			orionRequest = mapper.readValue(mMessage.getPayloadContent(), OrionRequest.class);
		} catch (JsonProcessingException e) {
			logger.error("Error while unpacking Orion response", e);
		}
		
		logger.info("ENG: End.");

		return ResponseEntity.ok()
				.headers(orionRequest.getHeaders())
				.body(orionRequest.getOriginalPayload());
	}

}

package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.idsa.dataapp.model.OrionRequest;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.service.OrionContextBrokerService;

@RestController
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "form")
public class DataControllerBodyForm {
	private static final Logger logger = LoggerFactory.getLogger(DataControllerBodyForm.class);

	private MultiPartMessageService multiPartMessageService;
	
	private OrionContextBrokerService orionService;
	
	public DataControllerBodyForm(MultiPartMessageService multiPartMessageService,
			OrionContextBrokerService orionService) {
		this.multiPartMessageService = multiPartMessageService;
		this.orionService = orionService;
    }

	@PostMapping(value = "/data")
	public ResponseEntity<?> routerForm(@RequestHeader HttpHeaders httpHeaders,
			@RequestParam(value = "header") String header,
			@RequestHeader(value = "Response-Type", required = false) String responseType,
			@RequestParam(value = "payload", required = false) String payload)
			throws UnsupportedOperationException, IOException {

		logger.info("Multipart/form request");

		// Received "header" and "payload"
		logger.info("header" + header);
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}
		
		ObjectMapper mapper = new ObjectMapper();

		OrionRequest orionRequest = mapper.readValue(payload, OrionRequest.class);
		ResponseEntity<String> response = orionService.enitityCall(orionRequest);

		OrionRequest orionResponse = new OrionRequest(response.getBody(), orionRequest.getMethod(), 
				response.getHeaders(), orionRequest.getRequestPath(), response.getStatusCode());
		
		// TODO create proper response message, depending on result operation
		// ArifactResponseMessage for GET - returning data ?
		// ResponseMessage - for all other operations
		HttpEntity resultEntity = multiPartMessageService.createMultipartMessageForm(
				multiPartMessageService.getResponseHeader(header),
				mapper.writeValueAsString(orionResponse),
				null,
				ContentType.APPLICATION_JSON);

		return ResponseEntity.ok().header("foo", "bar")
				.header(resultEntity.getContentType().getName(), 
						resultEntity.getContentType().getValue())
				.body(resultEntity.getContent().readAllBytes());
	}
}

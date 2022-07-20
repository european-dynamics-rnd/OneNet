package it.eng.idsa.dataapp.web.rest;

import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.IOException;
import java.net.URI;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ResultMessageBuilder;
import it.eng.idsa.dataapp.model.ContextBrokerException;
import it.eng.idsa.dataapp.model.OrionRequest;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

@Component
@ControllerAdvice
public class ContextBrokerErrorHandler {
	
	@Autowired
	private MultiPartMessageService multiPartMessageService;
	
	private ObjectMapper mapper = new ObjectMapper();

	@ExceptionHandler(ContextBrokerException.class)
	@ResponseBody
	public ResponseEntity<byte[]> handleError(ContextBrokerException e, HttpServletResponse response, 
			HttpServletRequest request) throws IOException {
		
		OrionRequest orionResponse = new OrionRequest(
				e.getHttpStatusCodeException().getResponseBodyAsString(), 
				e.getMethod(), 
				e.getHttpStatusCodeException().getResponseHeaders(), 
				request.getContextPath(),
				e.getHttpStatusCodeException().getStatusCode());
		
		HttpEntity resultEntity = multiPartMessageService.createMultipartMessageForm(
				UtilMessageService.getMessageAsString(createResultMessage(null)),
				mapper.writeValueAsString(orionResponse),
				null,
				ContentType.APPLICATION_JSON);
		
		return ResponseEntity.ok()
		.header(resultEntity.getContentType().getName(), 
				resultEntity.getContentType().getValue())
		.body(resultEntity.getContent().readAllBytes());
	
	}

	private Message createResultMessage(Message header) {
//		Mandatory fields are: securityToken, correlationMessage, issuerConnector, senderAgent, modelVersion, issued
		return new ResultMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())._issued_(DateUtil.now())
				._modelVersion_("4.1.0")
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAmEngRDProvider()))
				._correlationMessage_(header != null ? header.getId() : whoIAmEngRDProvider())
				._senderAgent_(whoIAmEngRDProvider())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				._issued_(DateUtil.now())
				.build();
	}

	private URI whoIAmEngRDProvider() {
		return URI.create("https://w3id.org/engrd/connector/provider");
	}

}

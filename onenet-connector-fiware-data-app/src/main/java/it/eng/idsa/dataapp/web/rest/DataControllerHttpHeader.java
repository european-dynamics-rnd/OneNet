package it.eng.idsa.dataapp.web.rest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import it.eng.idsa.dataapp.util.MessageUtil;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "http-header")
public class DataControllerHttpHeader {

	private static final Logger logger = LoggerFactory.getLogger(DataControllerHttpHeader.class);
	
	private MessageUtil messageUtil;
	
	public DataControllerHttpHeader(MessageUtil messageUtil) {
		this.messageUtil = messageUtil;
	}

	@PostMapping(value = "/data")
	public ResponseEntity<?> routerHttpHeader(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) String payload) {

		logger.info("Http Header request");
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload lenght = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}

		String requestMessageType = httpHeaders.getFirst("IDS-Messagetype");

		return ResponseEntity.ok().header("foo", "bar")
				.headers(createResponseMessageHeaders(requestMessageType))
				.header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
				.body(messageUtil.createResponsePayload(requestMessageType));
	}

	private HttpHeaders createResponseMessageHeaders(String requestMessageType) {
		HttpHeaders headers = new HttpHeaders();

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		String responseMessageType = null;

		if ("ids:ContractRequestMessage".equals(requestMessageType)) {
			responseMessageType = ContractAgreementMessage.class.getSimpleName();
		} else {
			responseMessageType = ArtifactResponseMessage.class.getSimpleName();
		}

		headers.add("IDS-Messagetype", "ids:" + responseMessageType);
		
		//changes first letter to lower case
		responseMessageType = Character.toLowerCase(responseMessageType.charAt(0)) + responseMessageType.substring(1);
		
		headers.add("IDS-Issued", formattedDate);
		headers.add("IDS-IssuerConnector", "http://w3id.org/engrd/connector");
		headers.add("IDS-CorrelationMessage", "https://w3id.org/idsa/autogen/"+ responseMessageType +"//"+ UUID.randomUUID().toString());
		headers.add("IDS-ModelVersion", "4.0.0");
		headers.add("IDS-Id", "https://w3id.org/idsa/autogen/"+ responseMessageType +"//"+ UUID.randomUUID().toString());

		return headers;
	}

}

package it.eng.idsa.dataapp.web.rest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.Message;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.streamer.WebSocketServerManager;

/**
 * @author Antonio Scatoloni
 */

public class IncomingDataAppResourceOverWs implements PropertyChangeListener {

	private static final Logger logger = LoggerFactory.getLogger(IncomingDataAppResourceOverWs.class);

	@Value("${application.dataLakeDirectory}")
	private Path dataLakeDirectory;

	@Autowired
	private MultiPartMessageService multiPartMessageService;
	
	@Autowired
	private MessageUtil messageUtil;
	
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		String requestMessageMultipart = (String) evt.getNewValue();
		Message requestMessage = multiPartMessageService.getMessage(requestMessageMultipart);
		String requestHeader = multiPartMessageService.getHeader(requestMessageMultipart);
		String requestedArtifact = null;
		String response = null;
		if (requestMessage instanceof ArtifactRequestMessage) {
			String reqArtifact = ((ArtifactRequestMessage) requestMessage).getRequestedArtifact().getPath();
			// get resource from URI http://w3id.org/engrd/connector/artifact/ + requestedArtifact
			requestedArtifact = reqArtifact.substring(reqArtifact.lastIndexOf('/') + 1);			
			logger.info("About to get file from " + requestedArtifact);
			response = readRequestedArtifact(requestMessage, requestedArtifact);
		} else if (requestMessage instanceof ContractRequestMessage) {
			response = contractAgreementResponse(requestMessage);
		} else {
			response = createDummyResponse(requestHeader);
		}
		WebSocketServerManager.getMessageWebSocketResponse().sendResponse(response);
	}

	private String contractAgreementResponse(Message requestMessage) {
		MultipartMessage responseMessageMultipart = new MultipartMessageBuilder()
				.withHeaderContent(multiPartMessageService
				.createContractAgreementMessage((ContractRequestMessage) requestMessage))
				.withPayloadContent(messageUtil.createResponsePayload(requestMessage))
				.build();
		return MultipartMessageProcessor.multipartMessagetoString(responseMessageMultipart, false);
	}

	private String createDummyResponse(String resquestMessage) {
		String responseMessageString = null;
		try {
			String responsePayload = createResponsePayload();
			// prepare multipart message.
			MultipartMessage responseMessage = new MultipartMessageBuilder()
					.withHeaderContent(resquestMessage)
					.withPayloadContent(responsePayload)
					.build();
			responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false);

		} catch (Exception e) {
			logger.error("Error while creating dummy response", e);
			Message rejectionMessage = multiPartMessageService.createRejectionMessageLocalIssues(multiPartMessageService.getMessage(resquestMessage));
			MultipartMessage responseMessageRejection = new MultipartMessageBuilder()
					.withHeaderContent(rejectionMessage)
					.withPayloadContent(null)
					.build();
			responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessageRejection, false);
		}
		return responseMessageString;
	}

	private String readRequestedArtifact(Message requestMessage, String requestedArtifact) {
		String responseMessageString = null;
		try {
			String responsePayload = readFile(requestedArtifact);
			String responseMessage = multiPartMessageService.getResponseHeader(requestMessage);
			// prepare multipart message.
			MultipartMessage responseMessageMultipart = new MultipartMessageBuilder()
					.withHeaderContent(responseMessage)
					.withPayloadContent(responsePayload)
					.build();
			responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessageMultipart, false);

		} catch (Exception e) {
			logger.error("Error while reading resource from disk", e);
			Message rejectionMessage = multiPartMessageService.createRejectionCommunicationLocalIssues(requestMessage);
			String payload = "{\r\n" + 
					"	\"reason\" : \"Resource '" + e.getMessage()  + "' not found\"\r\n" + 
					"}";
			MultipartMessage responseMessageRejection = new MultipartMessageBuilder()
					.withHeaderContent(rejectionMessage)
					.withPayloadContent(payload)
					.build();
			responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessageRejection, false);

		}
		return responseMessageString;
	}

	private String createResponsePayload() {
		// Put check sum in the payload
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		Map<String, String> jsonObject = new HashMap<>();
		jsonObject.put("firstName", "John");
		jsonObject.put("lastName", "Doe");
		jsonObject.put("dateOfBirth", formattedDate);
		jsonObject.put("address", "591  Franklin Street, Pennsylvania");
		jsonObject.put("checksum", "ABC123 " + formattedDate);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(jsonObject);
	}

	private String readFile(String requestedArtifact) throws IOException {
		logger.info("Reading file {} from datalake", requestedArtifact);
		byte[] fileContent = Files.readAllBytes(dataLakeDirectory.resolve(requestedArtifact));
		String base64EncodedFile = Base64.getEncoder().encodeToString(fileContent);
		logger.info("File read from disk.");
		return base64EncodedFile;
	}
}

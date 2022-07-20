package it.eng.idsa.dataapp.web.rest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ResponseMessage;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.service.RecreateFileService;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.streamer.WebSocketClientManager;
import it.eng.idsa.streamer.websocket.receiver.server.FileRecreatorBeanExecutor;

/**
 * @author Antonio Scatoloni
 */

@RestController
@EnableAutoConfiguration
//@RequestMapping({ "/" })
public class FileSenderResource {
	private static final Logger logger = LoggerFactory.getLogger(FileSenderResource.class);

	@Autowired
	MultiPartMessageServiceImpl multiPartMessageService;
	
	@Autowired
	RecreateFileService recreateFileService;
	
	@PostMapping("/requireandsavefile")
	@ResponseBody
	public String requireAndSaveFile(@RequestHeader("Forward-To-Internal") String forwardToInternal,
			@RequestHeader("Forward-To") String forwardTo, 
			@RequestBody String fileName) throws Exception {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples-multipart-messages/" + fileName);
		String message = IOUtils.toString(is, "UTF8");
		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = WebSocketClientManager.getMessageWebSocketSender()
				.sendMultipartMessageWebSocketOverHttps(message, forwardToInternal);

		String fileNameSaved = saveFileToDisk(responseMessage, message);

		String payload = "{​​\"message\":\"File '" + fileNameSaved + "' created successfully\"}";
		MultipartMessage multipartMessage = new MultipartMessage(
				new HashMap<>(), 
				new HashMap<>(),
				multiPartMessageService.getMessage(responseMessage),
				new HashMap<>(), 
				payload, 
				new HashMap<>(),
				null,
				null);
		return MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);

	}
	
	@PostMapping("/requirefile")
	@ResponseBody
	public String requireFile(@RequestHeader("Forward-To-Internal") String forwardToInternal,
			@RequestHeader("Forward-To") String forwardTo, 
			@RequestBody String fileName) throws Exception {
		InputStream is = this.getClass().getClassLoader().getResourceAsStream("examples-multipart-messages/" + fileName);
		String message = IOUtils.toString(is, "UTF8");
		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = WebSocketClientManager.getMessageWebSocketSender()
				.sendMultipartMessageWebSocketOverHttps(message, forwardToInternal);

		return responseMessage;

	}
	
	
	/**
	 * This method should not be used, check instead {@link it.eng.idsa.dataapp.web.rest.ProxyController#proxyRequest(HttpHeaders,
			 String, HttpMethod, HttpServletRequest, HttpServletResponse)}
	 * @param forwardToInternal
	 * @param forwardTo
	 * @param requestedArtifact
	 * @param payload
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/artifactRequestMessage")
	@ResponseBody
	@Deprecated
	public String requestArtifact(@RequestHeader("Forward-To-Internal") String forwardToInternal,
			@RequestHeader("Forward-To") String forwardTo, @RequestParam String requestedArtifact,
			@Nullable @RequestBody String payload) throws Exception {
		URI requestedArtifactURI = URI
				.create("http://w3id.org/engrd/connector/artifact/" + requestedArtifact);
		Message artifactRequestMessage = new ArtifactRequestMessageBuilder()
				._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
				._issuerConnector_(URI.create("http://w3id.org/engrd/connector"))
				._modelVersion_("4.0.0")
				._requestedArtifact_(requestedArtifactURI)
				.build();
		Serializer serializer = new Serializer();
		String requestMessage = serializer.serialize(artifactRequestMessage);
		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = WebSocketClientManager.getMessageWebSocketSender()
				.sendMultipartMessageWebSocketOverHttps(requestMessage, payload, forwardToInternal);

		String fileNameSaved = saveFileToDisk(responseMessage, artifactRequestMessage);

		String payloadResponse = null;
		if(fileNameSaved != null) {
			payloadResponse = "{​​\"message\":\"File '" + fileNameSaved + "' created successfully\"}";
		}
		MultipartMessage multipartMessage = new MultipartMessage(
				new HashMap<>(), 
				new HashMap<>(),
				multiPartMessageService.getMessage(responseMessage),
				new HashMap<>(), 
				payloadResponse, 
				new HashMap<>(),
				null,
				null);
		return MultipartMessageProcessor.multipartMessagetoString(multipartMessage, false);
	}
	
	private String saveFileToDisk(String responseMessage, String requestMessage) throws IOException {
		MultipartMessage request = MultipartMessageProcessor.parseMultipartMessage(requestMessage);
		MultipartMessage response = MultipartMessageProcessor.parseMultipartMessage(responseMessage);
		Message requestMsg = request.getHeaderContent();
		Message responseMsg = response.getHeaderContent();;
		logger.info("Response message: {} ", response.getHeaderContentString());
		String payload = response.getPayloadContent();
		logger.debug("Response payload: {} ", payload);

		String requestedArtifact = null;
		if (requestMsg instanceof ArtifactRequestMessage && responseMsg instanceof ResponseMessage) {
			requestedArtifact = ((ArtifactRequestMessage) requestMsg).getRequestedArtifact().getPath().split("/")[2];
			logger.info("About to save file " + requestedArtifact);
			recreateFileService.recreateTheFile(payload, new File(requestedArtifact));
			logger.info("File saved");
		} else {
			logger.info("Did not have ArtifactRequestMessage and ResponseMessage - nothing to save");
		}
		return requestedArtifact;
	}
	
	private String saveFileToDisk(String responseMessage, Message requestMessage) throws IOException {
		MultipartMessage response = MultipartMessageProcessor.parseMultipartMessage(responseMessage);
		Message responseMsg = response.getHeaderContent();

		String requestedArtifact = null;
		if (requestMessage instanceof ArtifactRequestMessage && responseMsg instanceof ArtifactResponseMessage) {
			String payload = response.getPayloadContent();
			String reqArtifact = ((ArtifactRequestMessage) requestMessage).getRequestedArtifact().getPath();
			// get resource from URI http://w3id.org/engrd/connector/artifact/ + requestedArtifact
			requestedArtifact = reqArtifact.substring(reqArtifact.lastIndexOf('/') + 1);
			logger.info("About to save file " + requestedArtifact);
			recreateFileService.recreateTheFile(payload, new File(requestedArtifact));
			logger.info("File saved");
		} else {
			logger.info("Did not have ArtifactRequestMessage and ResponseMessage - nothing to save");
			requestedArtifact = null;
		}
		return requestedArtifact;
	}
}

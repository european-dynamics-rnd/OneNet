package it.eng.idsa.dataapp.web.rest;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.dataapp.domain.MessageIDS;
import it.eng.idsa.dataapp.service.impl.MessageServiceImpl;
import it.eng.idsa.dataapp.service.impl.MultiPartMessageServiceImpl;


/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * REST controller for managing IncomingDataAppResource.
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@ConditionalOnProperty(
		value="application.websocket.isEnabled",
		havingValue = "false",
		matchIfMissing = true)
@RequestMapping({ "/incoming-data-app" })
public class IncomingDataAppResource {

	private static final Logger logger = LoggerFactory.getLogger(IncomingDataAppResource.class);

	@Autowired
	private MultiPartMessageServiceImpl multiPartMessageServiceImpl;

	@Autowired
	private MessageServiceImpl messageServiceImpl;
	
	/*
	@PostMapping(value="/dataAppIncomingMessage", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed", MediaType.ALL_VALUE }, produces= MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> receiveMessage(@RequestHeader (value="Content-Type", required=false) String contentType,  @RequestParam("header")  Object header,             
            @RequestParam("payload") Object payload   ) {
		logger.debug("POST /dataAppIncomingMessage");
		messageServiceImpl.setMessage(contentType, header.toString(), payload.toString());
		return ResponseEntity.ok().build();
	}
	 */

	@PostMapping("/dataAppIncomingMessageReceiver")
	public ResponseEntity<?> postMessageReceiver(@RequestBody String data){
		logger.info("Enter to the end-point: dataAppIncomingMessage Receiver side");
		String header=multiPartMessageServiceImpl.getHeader(data);
		String payload=multiPartMessageServiceImpl.getPayload(data);
		messageServiceImpl.setMessage("", header.toString(), payload.toString());
		logger.info("message="+data);
		return ResponseEntity.ok().build();
	}

	@PostMapping(value="/postMultipartMessage", produces= /*MediaType.MULTIPART_FORM_DATA_VALUE*/ MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> postMessage(@RequestHeader("Content-Type") String contentType,
			@RequestHeader("Forward-To") String forwardTo,  @RequestParam(value = "header",required = false)  Object header,             
			@RequestParam(value = "payload", required = false) Object payload   ) {
		logger.info("header"+header);
//		logger.info("payload="+payload);
		logger.info("forwardTo="+forwardTo);
		return new ResponseEntity<String>("postMultipartMessage endpoint: success\n", HttpStatus.OK);
	}
	

	@PostMapping("/dataAppIncomingMessageSender")
	public ResponseEntity<?> postMessageSender(@RequestBody String data){
		logger.info("Enter to the end-point: dataAppIncomingMessage Sender side");

		String header=multiPartMessageServiceImpl.getHeader(data);
		String payload=multiPartMessageServiceImpl.getPayload(data);
		messageServiceImpl.setMessage("", header.toString(), payload.toString());

		logger.info("message="+data);
		return ResponseEntity.ok().build();
	}

	@GetMapping("/dataAppIncomingMessage")
	public List<MessageIDS> testReceiveMessage() {
		logger.debug("GET /dataAppIncomingMessage");
		return messageServiceImpl.getMessages();
	}

}

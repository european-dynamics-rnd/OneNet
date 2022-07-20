package it.eng.idsa.dataapp.service.impl;


import static de.fraunhofer.iais.eis.util.Util.asList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.FormBodyPartBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.protocol.HTTP;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactResponseMessageBuilder;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessageBuilder;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.NotificationMessageBuilder;
import de.fraunhofer.iais.eis.RejectionMessageBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ResultMessageBuilder;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.TokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;

/**
 *
 * @author Milan Karajovic and Gabriele De Luca
 *
 */


/**
 * Service Implementation for managing MultiPartMessage.
 */
@Service
public class MultiPartMessageServiceImpl implements MultiPartMessageService {

	private static final Logger logger = LoggerFactory.getLogger(MultiPartMessageServiceImpl.class);
	
	@Value("${information.model.version}")
	private String informationModelVersion;

	@Override
	public String getHeader(String body) {
		MultipartMessage deserializedMultipartMessage = MultipartMessageProcessor.parseMultipartMessage(body);
		return deserializedMultipartMessage.getHeaderContentString();
	}

	@Override
	public String getPayload(String body) {
		MultipartMessage deserializedMultipartMessage = MultipartMessageProcessor.parseMultipartMessage(body);
		return deserializedMultipartMessage.getPayloadContent();
	}

	@Override
	public Message getMessage(String body) {
		MultipartMessage deserializedMultipartMessage = MultipartMessageProcessor.parseMultipartMessage(body);
		return deserializedMultipartMessage.getHeaderContent();
	}

	@Override
	public String addToken(Message message, String token) {
		String output = null;
		try {
			String msgSerialized = serializeMessage(message);
			Token tokenJsonValue = new TokenBuilder()
					._tokenFormat_(TokenFormat.JWT)
					._tokenValue_(token).build();
			String tokenValueSerialized=serializeMessage(tokenJsonValue);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			JSONObject jsonObjectToken = (JSONObject) parser.parse(tokenValueSerialized);
			jsonObject.put("securityToken",jsonObjectToken);
			output=serializeMessage(jsonObject);
		} catch (ParseException | IOException e) {
			logger.error("Error while parsing token - add", e);
		}
		return output;
	}

	@Override
	public String removeToken(Message message) {
		String output = null;
		try {
			String msgSerialized = serializeMessage(message);
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(msgSerialized);
			jsonObject.remove("securityToken");
			output=serializeMessage(jsonObject);
		} catch (ParseException | IOException e) {
			logger.error("Error while parsing token - remove", e);
		}
		return output;
	}

    @Override
    public String getResponseHeader(String header) {
	    Message message = null;
        if(null == header || header.isEmpty() || "null".equalsIgnoreCase(header)) {
            message = new NotificationMessageBuilder().build();
        } else
            message = getIDSMessage(header);
        return getResponseHeader(message);
    }

    @Override
    public String getResponseHeader(Message header) {
        String output = null;
        try {
            if(null == header || null == header.getId() || header.getId().toString().isEmpty())
                header = new NotificationMessageBuilder().build();
            if (header instanceof ArtifactRequestMessage){
                output = serializeMessage(createArtifactResponseMessage((ArtifactRequestMessage) header));
            } else if (header instanceof ContractRequestMessage) {
            	 output = serializeMessage(createContractAgreementMessage((ContractRequestMessage) header));
			} else if (header instanceof ContractAgreementMessage) {
           	 	output = serializeMessage(createProcessNotificationMessage((ContractAgreementMessage) header));
			} else {
                output = serializeMessage(createResultMessage(header));
            }
        } catch (IOException e) {
			logger.error("Error while processing response headers", e);
		}
		return output;
    }

	@Override
	public Message getMessage(Object header) {
		Message message = null;
		try {
			message = new Serializer().deserialize(String.valueOf(header), Message.class);
		} catch (IOException e) {
			logger.error("Error while deserializing message", e);
		}
		return message;
	}

	public Message getIDSMessage(String header) {
		Message message = null;
		try {
			message = new Serializer().deserialize(String.valueOf(header), Message.class);
		} catch (IOException e) {
			logger.error("Error while deserializing message", e);
		}
		return message;
	}

	@Override
	public HttpEntity createMultipartMessage(String header, String payload/*, String boundary, String contentType*/) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		try {
			multipartEntityBuilder.addTextBody("header", serializeMessage(createResultMessage(getIDSMessage(header))));
		} catch (JsonProcessingException e1) {
			try {
				multipartEntityBuilder.addTextBody("header", serializeMessage(createRejectionMessageLocalIssues(getMessage(header))));
			} catch (JsonProcessingException e) {
				multipartEntityBuilder.addTextBody("header", "INTERNAL ERROR");
				logger.error(e.getMessage());
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			logger.error(e1.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		if(payload!=null) {
			multipartEntityBuilder.addTextBody("payload", payload);
		}

		// multipartEntityBuilder.setBoundary(boundary)
		HttpEntity multipart = multipartEntityBuilder.build();

		//return multipart;
		InputStream streamHeader = new ByteArrayInputStream(header.getBytes(StandardCharsets.UTF_8));
		InputStream streamPayload = null;
		if(payload!=null) {
			streamPayload = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
		}

		multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.STRICT);
		try {
			FormBodyPart bodyHeaderPart;

			/*
			 * bodyHeaderPart = FormBodyPartBuilder.create() .addField(HTTP.CONTENT_LEN,
			 * ""+header.length()) .setName("header") .setBody(new StringBody(header))
			 * .build();
			 */
			ContentBody headerBody = new StringBody(header, ContentType.APPLICATION_JSON);
			bodyHeaderPart = FormBodyPartBuilder.create("header", headerBody).build();
			bodyHeaderPart.addField(HTTP.CONTENT_LEN, ""+header.length());

			FormBodyPart bodyPayloadPart=null;
			if(payload!=null) {
				ContentBody payloadBody = new StringBody(payload, ContentType.DEFAULT_TEXT);
				bodyPayloadPart = FormBodyPartBuilder.create("payload", payloadBody).build();
				bodyPayloadPart.addField(HTTP.CONTENT_LEN, "" + payload.length());
			}

			/*
			 * = FormBodyPartBuilder.create() .addField(HTTP.CONTENT_LEN,
			 * ""+payload.length()) .setName("payload") .setBody(new StringBody(payload))
			 * .build();
			 */


			multipartEntityBuilder.addPart (bodyHeaderPart);
			if(bodyPayloadPart!=null) {
				multipartEntityBuilder.addPart(bodyPayloadPart);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return multipartEntityBuilder.build();
	}
	
	@Override
	public HttpEntity createMultipartMessageForm(String header, String payload, String frowardTo, ContentType ctPayload) {
		MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
		multipartEntityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.STRICT);
		try {
			FormBodyPart bodyHeaderPart;
			ContentBody headerBody = new StringBody(header, ContentType.APPLICATION_JSON);
			bodyHeaderPart = FormBodyPartBuilder.create("header", headerBody).build();
			bodyHeaderPart.addField(HTTP.CONTENT_LEN, "" + header.length());
			multipartEntityBuilder.addPart(bodyHeaderPart);

			FormBodyPart bodyPayloadPart = null;
			if (payload != null) {
				ContentBody payloadBody = new StringBody(payload, ctPayload);
				bodyPayloadPart = FormBodyPartBuilder.create("payload", payloadBody).build();
				bodyPayloadPart.addField(HTTP.CONTENT_LEN, "" + payload.length());
				multipartEntityBuilder.addPart(bodyPayloadPart);
			}

			FormBodyPart headerForwardTo = null;
			if (frowardTo != null) {
				ContentBody forwardToBody = new StringBody(frowardTo, ContentType.DEFAULT_TEXT);
				headerForwardTo = FormBodyPartBuilder.create("forwardTo", forwardToBody).build();
				headerForwardTo.addField(HTTP.CONTENT_LEN, "" + frowardTo.length());
				multipartEntityBuilder.addPart(headerForwardTo);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return multipartEntityBuilder.build();
	}
	
	@Override
	public String getToken(String message) {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		String token = null;
		try {
			jsonObject = (JSONObject) parser.parse(message);
			jsonObject = (JSONObject) jsonObject.get("ids:securityToken");
			if (jsonObject == null) {
				logger.error(
						"Token is not set: securityToken is not set in the part of the header in the multipart message");
			} else {
				token = (String) jsonObject.get("ids:tokenValue");
				if (token == null) {
					logger.error(
							"Token is not set: tokenValue is not set in the part of the header in the multipart message");
				}
			}
		} catch (ParseException e) {
			logger.error("Error while getting token from message", e);
		}
		return token;
	}

	@Override
	public Message createResultMessage(Message header) {
		return new ResultMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				.build();
	}

	@Override
	public Message createArtifactResponseMessage(ArtifactRequestMessage header) {
		return new ArtifactResponseMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(UtilMessageService.MODEL_VERSION)
				._senderAgent_(whoIAmEngRDProvider())
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._securityToken_(UtilMessageService.getDynamicAttributeToken())
				.build();
	}
	
	@Override
	public Message createContractAgreementMessage(ContractRequestMessage header) {
		return new ContractAgreementMessageBuilder()
				._modelVersion_(informationModelVersion)
				._transferContract_(header.getTransferContract())
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._issued_(DateUtil.now())
				._issuerConnector_(whoIAmEngRDProvider())
				._recipientConnector_(Util.asList(header != null ? header.getIssuerConnector() : whoIAm()))
				.build();
	}


	public Message createRejectionMessage(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.MALFORMED_MESSAGE)
				.build();
	}

	public Message createRejectionToken(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.NOT_AUTHENTICATED)
				.build();
	}

	private URI whoIAm() {
		return URI.create("auto-generated");
	}
	
	private URI whoIAmEngRDProvider() {
		return URI.create("https://w3id.org/engrd/connector/provider");
	}
	
	private Message createProcessNotificationMessage(ContractAgreementMessage header) {
		return new MessageProcessedNotificationMessageBuilder()
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._issuerConnector_(whoIAmEngRDProvider())
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				.build();
	}
	

	public Message createRejectionMessageLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.MALFORMED_MESSAGE)
				.build();
	}

	public Message createRejectionTokenLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.NOT_AUTHENTICATED)
				.build();
	}


	public Message createRejectionCommunicationLocalIssues(Message header) {
		return new RejectionMessageBuilder()
				._issuerConnector_(whoIAmEngRDProvider())
				._issued_(DateUtil.now())
				._modelVersion_(informationModelVersion)
				._recipientConnector_(header != null ? asList(header.getIssuerConnector()) : asList(whoIAm()))
				._correlationMessage_(header != null ? header.getId() : whoIAm())
				._rejectionReason_(RejectionReason.NOT_FOUND)
				.build();
	}

    public static String serializeMessage(Object message) throws IOException {
        return MultipartMessageProcessor.serializeToJsonLD(message);
    }

}

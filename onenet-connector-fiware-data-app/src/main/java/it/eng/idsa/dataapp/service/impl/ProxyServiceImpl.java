package it.eng.idsa.dataapp.service.impl;

import static com.mongodb.client.model.Filters.eq;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ArtifactRequestMessageBuilder;
import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ConnectorUnavailableMessage;
import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.model.OrionRequest;
import it.eng.idsa.dataapp.service.ProxyService;
import it.eng.idsa.dataapp.service.RecreateFileService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import it.eng.idsa.multipart.util.DateUtil;
import it.eng.idsa.multipart.util.UtilMessageService;
import it.eng.idsa.streamer.WebSocketClientManager;
import it.eng.idsa.streamer.websocket.receiver.server.FileRecreatorBeanExecutor;

@Service
public class ProxyServiceImpl implements ProxyService {

	@Value("${application.mongo.host}") 
	private String host;

	@Value("${application.mongo.port}") 
	private String port;
	
	private static final String MULTIPART = "multipart";
	private static final String MESSAGE_TYPE = "messageType";
	private static final String PAYLOAD = "payload";
	private static final String REQUESTED_ARTIFACT = "requestedArtifact";
	private static final String FORWARD_TO = "Forward-To";
	private static final String ENTITY = "entity";
	private static final String PROVIDER_CONTEXT_BROKER = "Provider-Context-Broker";
	private static final String FORWARD_TO_INTERNAL = "Forward-To-Internal";
	private static final String REQUESTED_ELEMENT = "requestedElement";

	private static final String MESSAGE_AS_HEADERS = "messageAsHeaders";

	private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

	private RestTemplate restTemplate;
	private ECCProperties eccProperties;
	private RecreateFileService recreateFileService;
	private String dataLakeDirectory;
	
	public ProxyServiceImpl(RestTemplateBuilder restTemplateBuilder,  
			ECCProperties eccProperties,
			RecreateFileService recreateFileService,
			@Value("${application.dataLakeDirectory}") String dataLakeDirectory) {
		this.restTemplate = restTemplateBuilder.build();
		this.eccProperties = eccProperties;
		this.recreateFileService = recreateFileService;
		this.dataLakeDirectory = dataLakeDirectory;
	}
	
	@Override
	public ProxyRequest parseIncommingProxyRequest(String body) {
		JSONParser parser = new JSONParser();
		JSONObject jsonObject;
		try {
			jsonObject = (JSONObject) parser.parse(body);
			
			String multipart =  (String) jsonObject.get(MULTIPART);
			String forwardTo =  (String) jsonObject.get(FORWARD_TO);
			String forwardToInternal =  (String) jsonObject.get(FORWARD_TO_INTERNAL);
			String requestedArtifact = (String) jsonObject.get(REQUESTED_ARTIFACT);
			String messageType = (String) jsonObject.get(MESSAGE_TYPE);
			String requestedElement = (String) jsonObject.get(REQUESTED_ELEMENT);
			
			String payload = null;
			if(jsonObject.get(PAYLOAD) instanceof String) {
				payload = ((String) jsonObject.get(PAYLOAD)).replace("\\/","/").replace("\\", "");
			} else {
				JSONObject partJson = (JSONObject) jsonObject.get(PAYLOAD);
				payload =  partJson != null ? partJson.toJSONString().replace("\\/","/") : null;
			}
			
			logger.info("ENG: multipart         {}", multipart);
			logger.info("ENG: forwardTo         {}", forwardTo);
			logger.info("ENG: forwardToInternal {}", forwardToInternal);
			logger.info("ENG: payload           {}", payload);
			logger.info("ENG: requestedArtifact {}", requestedArtifact);
			logger.info("ENG: messageType       {}", messageType);
			logger.info("ENG: requestedElement  {}", requestedElement);
			
			return new ProxyRequest(multipart, forwardTo, forwardToInternal, payload, requestedArtifact, messageType, requestedElement);
		} catch (ParseException e) {
			logger.error("Error parsing payload", e);
		}
		return new ProxyRequest();
	}

	@Override
	public ResponseEntity<String> proxyMultipartMix(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		
		URI thirdPartyApi = null;
		String proxyPayload = null;
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());

		Message requestMessage = createRequestMessage(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement());
		
		if(requestMessage != null) {
			String payload = null;
			if(requestMessage instanceof ContractRequestMessage && proxyRequest.getPayload() == null) {
				logger.info("Creating ContractRequest for payload using requested artifact");
				payload = UtilMessageService.getMessageAsString(
						UtilMessageService.getContractRequest(URI.create(proxyRequest.getRequestedElement())));
			} else {
				logger.info("Using payload from request");
				payload = proxyRequest.getPayload();
			}
			
			MultipartMessage mm = new MultipartMessageBuilder()
					.withHeaderContent(requestMessage)
					.withPayloadContent(payload)
					.build();
			proxyPayload = MultipartMessageProcessor.multipartMessagetoString(mm, false, true);
			
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getMixContext(),
					null, null);
			
		} else if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerRegisterContext(),
					null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerDeleteContext(),
					null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerQuerryContext(),
					null, null);
			proxyPayload = proxyRequest.getPayload();
		}
		
		logger.info("Forwarding mix POST request to {}", thirdPartyApi.toString());
		
		HttpEntity<String> requestEntity = new HttpEntity<String>(proxyPayload, httpHeaders);
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	@Override
	public ResponseEntity<String> proxyMultipartForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		
		Message requestMessage = createRequestMessage(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement());
		URI thirdPartyApi = null;
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = null;
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());
		httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);

		logger.info("ENG: httpHeaders               {}", httpHeaders);
		logger.info("ENG: proxyRequest              {}", proxyRequest);
		logger.info("ENG: proxyRequest.getPayload() {}", proxyRequest.getPayload());

		if (requestMessage != null) {
			map.add("header", UtilMessageService.getMessageAsString(requestMessage));
			String payload = null;
			if(requestMessage instanceof ContractRequestMessage && proxyRequest.getPayload() == null) {
				logger.info("Creating ContractRequest for payload using requested artifact");
				payload = UtilMessageService.getMessageAsString(
						UtilMessageService.getContractRequest(URI.create(proxyRequest.getRequestedElement())));
			} else {
				logger.info("Using payload from request");
				payload = proxyRequest.getPayload();
			}
			map.add("payload", payload);
			
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getFormContext(),
					null, null);
		} else if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerRegisterContext(),
					null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerDeleteContext(),
					null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			map.add("payload", proxyRequest.getPayload());
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerQuerryContext(),
					null, null);
		}
		
		logger.info("Forwarding form POST request to {}", thirdPartyApi.toString());
		requestEntity = new HttpEntity<>(map, httpHeaders);
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}
	
	@Override
	public ResponseEntity<String> proxyGetEntityForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders, String contextPath) throws URISyntaxException {
		
		String ft_value = null;
		String pcb_value = null;
		
		Message requestMessage = createRequestMessage(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement());
		URI thirdPartyApi = null;
		HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = null;
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());
		httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
		
		logger.info("ENG: httpHeaders               {}", httpHeaders);
		logger.info("ENG: proxyRequest              {}", proxyRequest);
		logger.info("ENG: contextPath               {}", contextPath);
		logger.info("ENG: proxyRequest.getPayload() {}", proxyRequest.getPayload());
		
		String valueOfInterest = null;
		String[] myAppo = contextPath.split("/");
		int myLen = myAppo.length;
		if (myLen>0) {
			for (int index=0; index<myAppo.length; index++)
				logger.info("ENG: myAppo  <{}>, index <{}>", myAppo[index], index);
			
			valueOfInterest = myAppo[myLen-1];
			logger.info("ENG: valueOfInterest  <{}>", valueOfInterest);
			
		}
		else
			logger.error("ENG: contextPath field not present!");
			
		logger.info("ENG: start calling MongoClient");
		Document myDoc = null;
        try {
        	
//TODO: Ricavare le password da file di configurazione mongo-init.js        	
		    MongoClientURI uri = new MongoClientURI("mongodb://onenet-operation:true2022-operation@" + host + ":" + port + "/?authSource=orion");
		    MongoClient client = new MongoClient(uri);
		    logger.info("ENG: connecting to Database <orionHost> {} on port {}", host, port);
			if (client != null) {
			    logger.info("ENG: get database <orion>");
			    MongoDatabase db = client.getDatabase("orion");
			    
			    /*
			     * Create references collection if doesn't exists
			     */
			    boolean collectionExists = client.getDatabase("orion").listCollectionNames().into(new ArrayList<String>()).contains("references");
			    if (!collectionExists) {
				    logger.info("ENG: creation collection <references>");
			    	client.getDatabase("orion").createCollection("references");
			    }
			    else {
				    logger.info("ENG: collection <references> exists...");
			    }
			    
			    /* FORWARD_TO/PROVIDER-CONTEXT-BROKER */
			    logger.info("ENG: db.getCollection(\"references\")");
			    MongoCollection<Document> collection = db.getCollection("references");
		        
			    logger.info("ENG: collection.find()");
		        myDoc = collection.find(eq("entity", valueOfInterest)).first();
		        
			    logger.info("ENG: myDoc.get");
		        ft_value = myDoc.get(FORWARD_TO).toString();
			    logger.info("ENG: ft_value {}", ft_value);

			    pcb_value = myDoc.get(PROVIDER_CONTEXT_BROKER).toString();
			    logger.info("ENG: pcb_value {}", pcb_value);
		        
			    logger.info("ENG: ft_value && pcb_value != null?");
		        if (ft_value != null && pcb_value != null) {
				    logger.info("ENG: length of ft_value && pcb_value > 0?");
		        	if ( (ft_value.length()>0) && (pcb_value.length()>0) ) {
				        logger.info("ENG: myDocument entity                  <{}>", myDoc.get("entity"));			
				        logger.info("ENG: myDocument forward-to              <{}>", ft_value);			
				        logger.info("ENG: myDocument provider-context-broker <{}>", pcb_value);
		        	}
		        	else {
		        		logger.error("ENG: FORWARD_TO and PROVIDER_CONTEXT_BROKER length is 0 (zero). Mandatory values!");
		        	}
		        }
		        else {
	        		logger.error("ENG: FORWARD_TO and PROVIDER_CONTEXT_BROKER are NULL. Mandatory values!");		        	
		        }
		        
			}
			client.close();
			
		} catch (Exception e) {
			logger.error("ENG: error during call MongoClient {}", e.getMessage());
		}
        
		logger.info("ENG: end calling MongoClient");

		if (requestMessage != null) {
			map.add("header", UtilMessageService.getMessageAsString(requestMessage));
			String payload = null;
			if(requestMessage instanceof ContractRequestMessage && proxyRequest.getPayload() == null) {
				logger.info("Creating ContractRequest for payload using requested artifact");
				payload = UtilMessageService.getMessageAsString(
						UtilMessageService.getContractRequest(URI.create(proxyRequest.getRequestedElement())));
			} else {
				logger.info("Using payload from request");
				payload = proxyRequest.getPayload();
			}
			map.add("payload", payload);
			
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getFormContext(),
					null, null);
		} else if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerRegisterContext(),
					null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerDeleteContext(),
					null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			map.add("payload", proxyRequest.getPayload());
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerQuerryContext(),
					null, null);
		}
				
		logger.info("ENG: remove FORWARD_TO and PROVIDER_CONTEXT_BROCKER to httpHeaders");
		httpHeaders.remove(FORWARD_TO);
		httpHeaders.remove(PROVIDER_CONTEXT_BROKER);
		
		logger.info("ENG: adding FORWARD_TO and PROVIDER_CONTEXT_BROCKER to httpHeaders");
		httpHeaders.add(FORWARD_TO,  ft_value);
		httpHeaders.add (PROVIDER_CONTEXT_BROKER,  pcb_value);
		
		logger.info("ENG: new httpHeaders            {}", httpHeaders);
		logger.info("Forwarding form POST request to {}", thirdPartyApi.toString());
		requestEntity = new HttpEntity<>(map, httpHeaders);
		logger.info("ENG: requestEntity              {}", requestEntity);

		logger.info("ENG: calling restTemplate.exchange()...");
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
			
		logger.info("ENG: calling logResponse()...");
		logResponse(resp);
		
		return resp;
	}
	
	/*
	 * Metodo non utilizzato
	 */
	private boolean removeFromMongoDB (HttpHeaders httpHeaders) {
		
		boolean esito = false;
		
		logger.info("ENG: start calling MongoClient");
		Document myDoc = new Document();
        try {
		    logger.info("ENG: connecting to Database <orion>");
			MongoClient client = new MongoClient("192.168.1.85", 27017);
			if (client != null) {
			    logger.info("ENG: get database <orion>");
			    MongoDatabase db = client.getDatabase("orion");
			    
			    logger.info("ENG: db.getCollection(\"references\")");
			    MongoCollection<Document> collection = db.getCollection("references");
			    
			    logger.info("ENG: myDoc.put(ENTITY)");
			    myDoc.append(ENTITY, httpHeaders.getFirst(ENTITY));

			    logger.info("ENG: collection.deleteone(myDoc)");
			    collection.deleteOne(myDoc);
			    
				esito = true;
			}
			client.close();
			
		} catch (Exception e) {
			logger.error("ENG: error during call MongoClient {}", e.getMessage());
		}
        
		logger.info("ENG: end calling MongoClient");
		
		
		return esito;
	}
	
	private boolean storeIntoMongoDB (HttpHeaders httpHeaders) {
		
		boolean esito = false;
		
		logger.info("ENG: start calling MongoClient");
		Document myDoc = new Document();
        try {
		    logger.info("ENG: connecting to Database <orion>");

		  //TODO: Ricavare le password da file di configurazione mongo-init.js        	
		    MongoClientURI uri = new MongoClientURI("mongodb://onenet-operation:true2022-operation@" + host + ":" + port + "/?authSource=orion");
		    MongoClient client = new MongoClient(uri);
//		    MongoClient client = new MongoClient("onenet-operation:true2022-operation@" + host, Integer.valueOf(port));
//			MongoClient client = new MongoClient(host, Integer.valueOf(port));
			if (client != null) {
			    logger.info("ENG: get database <orion>");
			    MongoDatabase db = client.getDatabase("orion");
			    
			    /*
			     * Create references collection if doesn't exists
			     */
			    boolean collectionExists = client.getDatabase("orion").listCollectionNames().into(new ArrayList<String>()).contains("references");
			    if (!collectionExists) {
				    logger.info("ENG: creation collection <references>");
			    	client.getDatabase("orion").createCollection("references");
			    }
			    else {
				    logger.info("ENG: collection <references> exists...");
			    	
			    }
			    			    
			    /* FORWARD_TO/PROVIDER-CONTEXT-BROKER/ENTITY */
			    logger.info("ENG: db.getCollection(\"references\")");
			    MongoCollection<Document> collection = db.getCollection("references");
			    
			    //Write on Collection
			    logger.info("ENG: myDoc.put(FORWARD_TO)");
			    myDoc.append(FORWARD_TO, httpHeaders.getFirst(FORWARD_TO));
		        
			    logger.info("ENG: myDoc.put(PROVIDER_CONTEXT_BROKER)");
			    myDoc.append(PROVIDER_CONTEXT_BROKER, httpHeaders.getFirst(PROVIDER_CONTEXT_BROKER));

			    logger.info("ENG: myDoc.put(ENTITY)");
			    myDoc.append(ENTITY, httpHeaders.getFirst(ENTITY));
			    
			    logger.info("ENG: myDoc <{}>", myDoc.toString());			    
			    
			    logger.info("ENG: collection.insertOne(myDoc)");
			    collection.insertOne(myDoc);
			    
				esito = true;
			}
			client.close();
			
		} catch (Exception e) {
			logger.error("ENG: error during call MongoClient {}", e.getMessage());
		}
        
		logger.info("ENG: end calling MongoClient");

		return esito;
		
	}
	
	@Override
	public ResponseEntity<String> proxyRegistrationEntityForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders, String contextPath, URI myUri) throws URISyntaxException {
		
		Message requestMessage = createRequestMessage(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement());
		URI thirdPartyApi = null;
		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		
		logger.info("ENG: httpHeaders               {}", httpHeaders);
		logger.info("ENG: proxyRequest              {}", proxyRequest);
		logger.info("ENG: contextPath               {}", contextPath);
		logger.info("ENG: proxyRequest.getPayload() {}", proxyRequest.getPayload());

		String payload = null;
		if (requestMessage != null) {
			map.add("header", UtilMessageService.getMessageAsString(requestMessage));
			if(requestMessage instanceof ContractRequestMessage && proxyRequest.getPayload() == null) {
				logger.info("Creating ContractRequest for payload using requested artifact");
				payload = UtilMessageService.getMessageAsString(
						UtilMessageService.getContractRequest(URI.create(proxyRequest.getRequestedElement())));
			} else {
				logger.info("Using payload from request");
				payload = proxyRequest.getPayload();
			}
			map.add("payload", payload);
			
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getFormContext(),
					null, null);
		} else if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerRegisterContext(),
					null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerDeleteContext(),
					null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			map.add("payload", proxyRequest.getPayload());
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerQuerryContext(),
					null, null);
		}
		thirdPartyApi =  myUri;
				
		logger.info("ENG: new payload                {}", payload);
		map.add("payload", payload);
		
		logger.info("ENG: new httpHeaders            {}", httpHeaders);
		logger.info("Forwarding form POST request to {}", thirdPartyApi.toString());
		HttpEntity<String> requestEntity2 = new HttpEntity<>(payload, httpHeaders);
		logger.info("ENG: requestEntity              {}", requestEntity2);

		logger.info("ENG: calling restTemplate.exchange()...");
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity2, String.class);
			
		logger.info("ENG: calling logResponse()...");
		logResponse(resp);

//		//TODO Come gestire valore di ritorno per eventuale 
//		//ROLLBACK chiamando removeFromMongoDB() ?
		if (resp.getStatusCodeValue() == 201) {
			/*
			 * Memorizza la tupla FORWARD_TO, PROVIDER_CONTEXT_BROKER e ENTITY
			 * nella collection references di MongoDB orion
			 */
			boolean esito = storeIntoMongoDB (httpHeaders);
			if (!esito) {
				logger.error("ENG: Storing on MongoDB error...");
			}
			else {
				logger.info("ENG: Storing on MongoDB OK!");
			}
		}
		
		logger.info("ENG: resp.getStatusCode()      {}", resp.getStatusCode());
		logger.info("ENG: resp.getStatusCodeValue() {}", resp.getStatusCodeValue());
		
		logResponse (resp);
		
		return resp;
	}
	
	@Override
	public ResponseEntity<String> proxyHttpHeader(ProxyRequest proxyRequest, HttpHeaders httpHeaders)
			throws URISyntaxException {
		URI thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
				eccProperties.getPort(), eccProperties.getHeaderContext(),
				null, null);

		logger.info("Forwarding header POST request to {}", thirdPartyApi.toString());
		httpHeaders.addAll(createMessageAsHeader(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement()));
		httpHeaders.add(FORWARD_TO, proxyRequest.getForwardTo());
		
		String payload = null;
		if(proxyRequest.getMessageType().contains("ContractRequestMessage") && proxyRequest.getPayload() == null) {
			logger.info("Creating ContractRequest for payload using requested artifact");
			payload = UtilMessageService.getMessageAsString(
					UtilMessageService.getContractRequest(URI.create(proxyRequest.getRequestedElement())));
		} else {
			logger.info("Using payload from request");
			payload = proxyRequest.getPayload();
		}
		HttpEntity<String> requestEntity = new HttpEntity<>(payload, httpHeaders);
		
		if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - ConnectorUpdateMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerRegisterContext(),
					null, null);
		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
		 	logger.info("Broker message - ConnectorUnavailableMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerDeleteContext(),
					null, null);
		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
			logger.info("Broker message - QueryMessage");
			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
					eccProperties.getPort(), eccProperties.getBrokerQuerryContext(),
					null, null);
		}
		
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity, String.class);
		logResponse(resp);
		return resp;
	}

	@Override
	public ResponseEntity<String> requestArtifact(ProxyRequest proxyRequest){
		String forwardToInternal = proxyRequest.getForwardToInternal();
		String forwardTo = proxyRequest.getForwardTo();
		
		if(StringUtils.isEmpty(forwardTo) || StringUtils.isEmpty(forwardToInternal)) {
			return ResponseEntity.badRequest().body("Missing required fields Forward-To or Forward-To-Internal");
		}
		
		URI requestedArtifactURI = URI
				.create("http://w3id.org/engrd/connector/artifact/" + proxyRequest.getRequestedArtifact());
		Message artifactRequestMessage;
		try {
			artifactRequestMessage = new ArtifactRequestMessageBuilder()
					._issued_(DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar()))
					._issuerConnector_(URI.create("http://w3id.org/engrd/connector"))
					._modelVersion_("4.0.0")
					._requestedArtifact_(requestedArtifactURI)
					.build();

			Serializer serializer = new Serializer();
			String requestMessage = serializer.serialize(artifactRequestMessage);
			FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
			String responseMessage = WebSocketClientManager.getMessageWebSocketSender()
					.sendMultipartMessageWebSocketOverHttps(requestMessage, proxyRequest.getPayload(), forwardToInternal);
			
			String fileNameSaved = saveFileToDisk(responseMessage, artifactRequestMessage);
			
			if(fileNameSaved != null) {
				return ResponseEntity.ok("{​​\"message\":\"File '" + fileNameSaved + "' created successfully\"}");
			}
			return ResponseEntity.ok(responseMessage);
		} catch (Exception exc) {
			logger.error("Error while processing request {}", exc);
			 throw new ResponseStatusException(
			           HttpStatus.INTERNAL_SERVER_ERROR, 
			           "Error while processing request, check logs for more details", 
			           exc);
		}
	}
	
	private Message createRequestMessage(String messageType, String requestedArtifact, String requestedElement) {
		if(ArtifactRequestMessage.class.getSimpleName().equals(messageType)) {
			return UtilMessageService.getArtifactRequestMessage(requestedArtifact != null 
					? URI.create(requestedArtifact) 
							: UtilMessageService.REQUESTED_ARTIFACT);
		} else if(ContractAgreementMessage.class.getSimpleName().equals(messageType)) {
			return UtilMessageService.getContractAgreementMessage();
		} else if(ContractRequestMessage.class.getSimpleName().equals(messageType)) {
			return UtilMessageService.getContractRequestMessage();
		} else if(DescriptionRequestMessage.class.getSimpleName().equals(messageType)) {
			URI reqEl = requestedElement == null ? null : URI.create(requestedElement);
			return UtilMessageService.getDescriptionRequestMessage(reqEl);
		} 
		return null;
	}
	
	private HttpHeaders createMessageAsHeader(String messageType, String requestedArtifact, String requestedElement) {
		HttpHeaders httpHeaders = new HttpHeaders();
		if(ArtifactRequestMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:" + ArtifactRequestMessage.class.getSimpleName());
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + ArtifactRequestMessage.class.getSimpleName() + "/" + UUID.randomUUID());
			httpHeaders.add("IDS-RequestedArtifact", requestedArtifact != null ? requestedArtifact : UtilMessageService.REQUESTED_ARTIFACT.toString());
		} else if(ContractRequestMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:" + ContractRequestMessage.class.getSimpleName());
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + ContractRequestMessage.class.getSimpleName() + "/" + UUID.randomUUID());
		} else if(ContractAgreementMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:" + ContractAgreementMessage.class.getSimpleName());
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + ContractAgreementMessage.class.getSimpleName() + "/" + UUID.randomUUID());
		} else if(DescriptionRequestMessage.class.getSimpleName().equals(messageType)) {
			httpHeaders.add("IDS-Messagetype", "ids:" + DescriptionRequestMessage.class.getSimpleName());
			httpHeaders.add("IDS-Id", "https://w3id.org/idsa/autogen/" + DescriptionRequestMessage.class.getSimpleName() + "/" + UUID.randomUUID());
			httpHeaders.add("IDS-RequestedElement", requestedElement);
		}
		
		httpHeaders.add("IDS-ModelVersion", "4.1.0");
        httpHeaders.add("IDS-TransferContract", UtilMessageService.TRANSFER_CONTRACT.toString());
		httpHeaders.add("IDS-Issued", DateUtil.now().toXMLFormat());
		httpHeaders.add("IDS-IssuerConnector", "http://w3id.org/engrd/connector/");
		httpHeaders.add("IDS-SenderAgent", "http://sender.agent.com/");
		
		httpHeaders.add("IDS-SecurityToken-Type", "ids:DynamicAttributeToken");
		httpHeaders.add("IDS-SecurityToken-Id", "https://w3id.org/idsa/autogen/" + UUID.randomUUID());
		httpHeaders.add("IDS-SecurityToken-TokenFormat", TokenFormat.JWT.getId().toString());
		httpHeaders.add("IDS-SecurityToken-TokenValue", UtilMessageService.TOKEN_VALUE);
		
		return httpHeaders;
	}
	
	private void logResponse(ResponseEntity<String> resp) {
		logger.info("Response received with status code {}", resp.getStatusCode());
		logger.info("Response headers\n{}", resp.getHeaders());
		logger.info("Response body\n{}", resp.getBody());
	}
	
	// TODO should we move this method to separate class?
	private String saveFileToDisk(String responseMessage, Message requestMessage) throws IOException {
		MultipartMessage response = MultipartMessageProcessor.parseMultipartMessage(responseMessage);
		Message responseMsg = response.getHeaderContent();

		String requestedArtifact = null;
		if (requestMessage instanceof ArtifactRequestMessage && responseMsg instanceof ArtifactResponseMessage) {
			String payload = response.getPayloadContent();
			String reqArtifact = ((ArtifactRequestMessage) requestMessage).getRequestedArtifact().getPath();
			// get resource from URI http://w3id.org/engrd/connector/artifact/ + requestedArtifact
			requestedArtifact = reqArtifact.substring(reqArtifact.lastIndexOf('/') + 1);
			String dataLake = dataLakeDirectory + FileSystems.getDefault().getSeparator() + requestedArtifact;
			logger.info("About to save file " + dataLake);
			recreateFileService.recreateTheFile(payload, new File(dataLake));
			logger.info("File saved");
		} else {
			logger.info("Did not have ArtifactRequestMessage and ResponseMessage - nothing to save");
			requestedArtifact = null;
		}
		return requestedArtifact;
	}

	@Override
	public ResponseEntity<String> proxyWSSRequest(ProxyRequest proxyRequest) {
		String forwardToInternal = proxyRequest.getForwardToInternal();
		String forwardTo = proxyRequest.getForwardTo();
		
		if(StringUtils.isEmpty(forwardTo) || StringUtils.isEmpty(forwardToInternal)) {
			return ResponseEntity.badRequest().body("Missing required fields Forward-To or Forward-To-Internal");
		}
		
		FileRecreatorBeanExecutor.getInstance().setForwardTo(forwardTo);
		String responseMessage = null;
		try {
			responseMessage = WebSocketClientManager.getMessageWebSocketSender()
					.sendMultipartMessageWebSocketOverHttps(proxyRequest.getMessageType(), proxyRequest.getPayload(), forwardToInternal);
		} catch (Exception exc) {
			logger.error("Error while processing request {}", exc);
			 throw new ResponseStatusException(
			           HttpStatus.INTERNAL_SERVER_ERROR, 
			           "Error while processing request, check logs for more details", 
			           exc);
		}
		
		return ResponseEntity.ok(responseMessage);
	}

	@Override
	public ResponseEntity<String> convertToOrionResponse(ResponseEntity<String> resultEntity) {
		logger.info("ENG: Start...");

		logger.info("ENG: resultEntity.getHeaders <{}>", resultEntity.getHeaders());
		logger.info("ENG: resultEntity.getBody    <{}>", resultEntity.getBody());

		ObjectMapper mapper = new ObjectMapper();
		OrionRequest orionRequest = null;
		
		logger.info("ENG: calling MultipartMessageProcessor.parseMultipartMessage...");
		MultipartMessage mMessage = MultipartMessageProcessor.parseMultipartMessage(resultEntity.getBody());
		logger.info("ENG: result is <{}>", mMessage);
		
		try {
			logger.info("ENG: mMessage.getPayloadContent() {}", mMessage.getPayloadContent());
			logger.info("ENG: calling mapper.readValue()...");
			orionRequest = mapper.readValue(mMessage.getPayloadContent(), OrionRequest.class);
			logger.info("ENG: result is orionRequest <{}>", orionRequest);
		} catch (JsonProcessingException e) {
			logger.error("Error while unpacking Orion response", e);
		}
		
		logger.info("ENG: End.");

		return ResponseEntity.ok()
				.headers(orionRequest.getHeaders())
				.body(orionRequest.getOriginalPayload());
	}
	
	@Override
	public ResponseEntity<String> convertToDataAppResponse(ResponseEntity<String> resultEntity) {
		logger.info("ENG: Start...");

		logger.info("ENG: resultEntity.getHeaders <{}>", resultEntity.getHeaders());
		logger.info("ENG: resultEntity.getBody    <{}>", resultEntity.getBody());

		ObjectMapper mapper = new ObjectMapper();
		OrionRequest orionRequest = null;

		logger.info("ENG: calling MultipartMessageProcessor.parseMultipartMessage...");
		MultipartMessage mMessage = MultipartMessageProcessor.parseMultipartMessage(resultEntity.getBody());
		logger.info("ENG: result is <{}>", mMessage);
		
		try {
			logger.info("ENG: mMessage.getPayloadContent() {}", mMessage.getPayloadContent());
			logger.info("ENG: calling mapper.readValue()...");
			orionRequest = mapper.readValue(mMessage.getPayloadContent(), OrionRequest.class);
			logger.info("ENG: result is orionRequest <{}>", orionRequest);
			
		} catch (JsonProcessingException e) {
			logger.error("Error while unpacking Orion response", e);
		}
		
		logger.info("ENG: End.");

		return ResponseEntity.ok()
				.headers(orionRequest.getHeaders())
				.body(orionRequest.getOriginalPayload());
	}

	@Override
	public ResponseEntity<?> proxyCreationEntityForm(HttpHeaders httpHeaders, String body, URI myUri) throws URISyntaxException {
//		Message requestMessage = createRequestMessage(proxyRequest.getMessageType(), proxyRequest.getRequestedArtifact(), proxyRequest.getRequestedElement());
		URI thirdPartyApi = null;
//		LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		
		logger.info("ENG: httpHeaders               {}", httpHeaders);
//		logger.info("ENG: proxyRequest              {}", proxyRequest);
//		logger.info("ENG: proxyRequest.getPayload() {}", proxyRequest.getPayload());

//		String payload = null;
//		if (requestMessage != null) {
//			map.add("header", UtilMessageService.getMessageAsString(requestMessage));
//			if(requestMessage instanceof ContractRequestMessage && proxyRequest.getPayload() == null) {
//				logger.info("Creating ContractRequest for payload using requested artifact");
//				payload = UtilMessageService.getMessageAsString(
//						UtilMessageService.getContractRequest(URI.create(proxyRequest.getRequestedElement())));
//			} else {
//				logger.info("Using payload from request");
//				payload = proxyRequest.getPayload();
//			}
//			map.add("payload", payload);
//			
//			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
//					eccProperties.getPort(), eccProperties.getFormContext(),
//					null, null);
//		} else if (ConnectorUpdateMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
//			logger.info("Broker message - ConnectorUpdateMessage");
//			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
//					eccProperties.getPort(), eccProperties.getBrokerRegisterContext(),
//					null, null);
//		} else if (ConnectorUnavailableMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
//			logger.info("Broker message - ConnectorUnavailableMessage");
//			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
//					eccProperties.getPort(), eccProperties.getBrokerDeleteContext(),
//					null, null);
//		} else if (QueryMessage.class.getSimpleName().equals(proxyRequest.getMessageType())) {
//			logger.info("Broker message - QueryMessage");
//			map.add("payload", proxyRequest.getPayload());
//			thirdPartyApi = new URI(eccProperties.getProtocol(), null, eccProperties.getHost(), 
//					eccProperties.getPort(), eccProperties.getBrokerQuerryContext(),
//					null, null);
//		}

//		thirdPartyApi =  new URI ("http", null, "192.168.1.85", 1027, "/ngsi-ld/v1/entities/", null, null);
		thirdPartyApi =  myUri;
				
		logger.info("ENG:  body                      {}", body);
		logger.info("ENG: new httpHeaders            {}", httpHeaders);
		logger.info("Forwarding form POST request to {}", thirdPartyApi.toString());
		HttpEntity<String> requestEntity2 = new HttpEntity<>(body, httpHeaders);
		logger.info("ENG: requestEntity              {}", requestEntity2);

		logger.info("ENG: calling restTemplate.exchange()...");
		ResponseEntity<String> resp = restTemplate.exchange(thirdPartyApi, HttpMethod.POST, requestEntity2, String.class);
			
		logger.info("ENG: calling logResponse()...");
		logResponse(resp);

		return resp;
	}
}

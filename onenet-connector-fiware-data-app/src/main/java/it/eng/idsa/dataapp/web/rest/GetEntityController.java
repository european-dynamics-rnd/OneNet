package it.eng.idsa.dataapp.web.rest;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.model.GetEntityRequest;
import it.eng.idsa.dataapp.service.ProxyService;

@RestController

public class GetEntityController {

	private static final Logger logger = LoggerFactory.getLogger(GetEntityController.class);
	
	@Autowired
	private ProxyService proxyService;
	
	@Value("${application.orion.host}")
	private String orionHost;

	@Value("${application.fiware.ecc.provider.url}") 
	private String providerURL;

	@Value("${application.fiware.contextpath.orionprovider}") 
	private String contextPathOrionProvider;
	
	@Value("${application.fiware.contextpath.getentity}") 
	private String contextPathGetEntity;
	
	@RequestMapping("/getentity/**")
	public ResponseEntity<?> proxyToDataApp(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) String body, HttpMethod method, HttpServletRequest request) throws URISyntaxException, JsonProcessingException {
		logger.info("HTTP Method {}", method.name());
		logger.info("HTTP Headers {}", httpHeaders.entrySet().stream()
				.map(Map.Entry::toString)
				.collect(Collectors.joining(";", "[", "]")));
		
		logger.info("ENG: request.getRequestURI()  {}", request.getRequestURI());
		logger.info("ENG: contextPathOrionProvider {}", contextPathOrionProvider);
		logger.info("ENG: contextPathGetEntity     {}", contextPathGetEntity);
		
		//Replace contextPathGetEntity with contextPathOrionProvider
		String myAppo = new String (request.getRequestURI());
		myAppo = myAppo.replace(contextPathGetEntity, contextPathOrionProvider);
		StringBuilder contextPath = new StringBuilder(myAppo);
		logger.info("ENG: Path {}", contextPath.toString());
		if(StringUtils.isNotBlank(request.getQueryString())) {
			contextPath.append("?")
				.append(request.getQueryString());
		}
		logger.info("payload \n{}", body);
		
		String strForwardTo = "";
		List<String> myForwardTo = httpHeaders.getValuesAsList("forward-to");
		if (myForwardTo.isEmpty()) {
			logger.error ("forward-to IS EMPTY");
		}
		else
			strForwardTo = myForwardTo.toString();
//
//		String strPCB = "";
//		List<String> myPCB = httpHeaders.getValuesAsList("provider-context-broker");
//		if (myPCB.isEmpty())
//			logger.error ("provider-context-broker IS EMPTY");
//		else
//			strPCB = myPCB.toString();
//		
//		logger.info ("forward-to              {}", myForwardTo.toString());
//		logger.info ("provider-context-broker {}", myPCB.toString());
		logger.info ("httpHeaders             {}", httpHeaders);
		
		GetEntityRequest getEntityRequest = new GetEntityRequest(body, method, httpHeaders, contextPath.toString());

		logger.info("ENG: providerURL {}", providerURL);
		
		ObjectMapper mapper = new ObjectMapper();
		ProxyRequest proxyRequest = new ProxyRequest(ProxyRequest.MULTIPART_FORM, 
				providerURL, 
				strForwardTo, 
				// TODO update logic to pass some ID that will be used for UsageControl as requestedArtifact
				mapper.writeValueAsString(getEntityRequest), 
				null, 
				ArtifactRequestMessage.class.getSimpleName(), 
				null);
		
		return proxyService.convertToOrionResponse(proxyService.proxyGetEntityForm(proxyRequest, httpHeaders, contextPath.toString()));
//		return proxyService.convertToDataAppResponse(proxyService.proxyGetEntityForm(proxyRequest, httpHeaders, contextPath.toString()));
	}
	
}

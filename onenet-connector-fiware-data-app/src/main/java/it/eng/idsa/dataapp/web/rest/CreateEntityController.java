package it.eng.idsa.dataapp.web.rest;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.service.ProxyService;

@RestController

public class CreateEntityController {

	private static final Logger logger = LoggerFactory.getLogger(CreateEntityController.class);
	
	@Autowired
	private ProxyService proxyService;
	
	@Value("${application.fiware.ecc.provider.url}") 
	private String providerURL;

	@Value("${application.fiware.contextBroker.provider.url}") 
	private String contextBrokerProviderUrl;
	
	@Value("${application.fiware.contextpath.orioncreateentity}") 
	private String orionCreateEntity;
	
	@Value("${application.fiware.contextpath.createentity}") 
	private String createEntity;
	
	@Value("${application.orion.protocol}")
	private String orionProtocol;
	
	@Value("${application.orion.host}")
	private String orionHost;

	@Value("${application.orion.port.createentity}")
	private String orionPort;

	@RequestMapping("/createentity/**")
	public ResponseEntity<?> proxyToDataApp(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = true) String body, HttpMethod method, HttpServletRequest request) throws URISyntaxException, Exception {
		logger.info("HTTP Method {}", method.name());
		logger.info("HTTP Headers {}", httpHeaders.entrySet().stream().map(Map.Entry::toString).collect(Collectors.joining(";", "[", "]")));
		
		logger.info("ENG: request.getRequestURI()       {}", request.getRequestURI());
		logger.info("ENG: contextPathOrionRegistration  {}", orionCreateEntity);
		logger.info("ENG: contextPathRegistrationEntity {}", createEntity);
		
		//Replace contextPathGetEntity with contextPathOrionProvider
		String myAppo = new String (request.getRequestURI());
		myAppo = myAppo.replace(createEntity, orionCreateEntity);
		StringBuilder contextPath = new StringBuilder(myAppo);
		logger.info("ENG: Path {}", contextPath.toString());
		if(StringUtils.isNotBlank(request.getQueryString())) {
			contextPath.append("?")
				.append(request.getQueryString());
		}
		logger.info("ENG: httpHeaders {}", httpHeaders);
		logger.info("ENG: method      {}", method);
		logger.info("ENG: request     {}", request);
		logger.info("ENG: body        {}", body);
		logger.info ("contextPath     {}", contextPath);

		logger.info("ENG: contextBrokerProviderUrl {}", contextBrokerProviderUrl);
		logger.info("ENG: ecc.provider.url         {}", providerURL);
		
		httpHeaders.add("Content-Length", String.valueOf(body.length()));
		httpHeaders.add("Content-Type", "application/ld+json");
		ProxyRequest proxyRequest = new ProxyRequest();
		logger.info("ENG: proxyRequest.getPayload() {}", proxyRequest.getPayload());

		logger.info("Forwarding request using {}", ProxyRequest.MULTIPART_MIXED);
		
		URI myUri =  new URI (orionProtocol, null, orionHost, Integer.valueOf(orionPort), "/" + orionCreateEntity + "/", null, null);
		
		return proxyService.proxyCreationEntityForm(httpHeaders, body, myUri);
		
	}
	
	/*
	 * Metodo di TEST
	 * To test this function put a GET post from your browser at http://192.168.1.85:8084/test
	 */
//	@Value("classpath:data/mongo-init.js")
	@Value("classpath:C:/Users/S225885/git/onenet-connector-fiware-data-app/doc/mongo-init.js")
	Resource resourceFile;
	
	@Autowired
	Environment environment;

	@GetMapping("/test/**")
	String testConnection(){

		logger.info("ENG: Your server host    is {}", environment.getProperty("local.server.host"));
		logger.info("ENG: Your server port    is {}", environment.getProperty("local.server.port"));
		logger.info("ENG: Your server address is {}", environment.getProperty("server.address"));
		logger.info("ENG: Your server port    is {}", environment.getProperty("server.port"));
		
		logger.info("ENG: resourceFile        is {}", resourceFile);

		try {
        	// File path is passed as parameter
//        	Map<String, String> userData;
//            File file = new File("./mongo-init.js");
//            BufferedReader br = new BufferedReader(new FileReader(file));
//            String st;
//            while ((st = br.readLine()) != null)
//            	logger.info("ENG: read line: {}", st);
         	

//            File resource = new ClassPathResource("C:/Users/S225885/git/onenet-connector-fiware-data-app/doc/mongo-init.js").getFile();
//            String employees = new String(Files.readAllBytes(resource.toPath()));
//            logger.info ("ENG: read file {}", employees);
            
			File myFile = resourceFile.getFile();
            logger.info ("ENG: myFile {}", myFile);
            
			String myAbs = myFile.getAbsolutePath();
            logger.info ("ENG: myAbs  {}", myAbs);
			
            InputStream resource = new ClassPathResource(myAbs).getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource));
            String employees = reader.lines().collect(Collectors.joining("\n"));
            logger.info ("ENG: read file {}", employees);

            
//			FileSystemResource imgFile = new FileSystemResource("C:/Users/S225885/git/onenet-connector-fiware-data-app/doc/mongo-init.js");
//			BufferedReader br = 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error ("ENG: FileSystemResource {}", e.getMessage());
		}
		
	    return "Test done...";      
	}

}

package it.eng.idsa.dataapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.domain.ProxyRequest;
import it.eng.idsa.dataapp.service.impl.ProxyServiceImpl;
import it.eng.idsa.multipart.util.UtilMessageService;

public class ProxyServiceTest {

	private static final String PAYLOAD = "Payload";
	private String messageType;

	private ProxyServiceImpl service;
	@Mock
	private RecreateFileService recreateFileService;
	@Mock
	private HttpHeaders httpHeaders;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private RestTemplateBuilder restTemplateBuilder;
	@Mock
	private ECCProperties eccProperties;
	@Mock
	private ProxyRequest proxyRequest;
	@Mock
	private ResponseEntity<String> response;
	private Map<String, Object> messageAsHeaders;
	private String dataLakeDirectory ;
	
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		when(restTemplateBuilder.build()).thenReturn(restTemplate);
		service = new ProxyServiceImpl(restTemplateBuilder, eccProperties, recreateFileService, dataLakeDirectory);
		messageType = ArtifactRequestMessage.class.getSimpleName();
		when(eccProperties.getProtocol()).thenReturn("https");
		when(eccProperties.getHost()).thenReturn("test.host");
		when(eccProperties.getPort()).thenReturn(123);
		mockMessageAsHeaders();
	}

	@Test
	public void proxyMultipartMix_Success() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_MIXED);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		mockResponse(HttpStatus.OK);
		
		service.proxyMultipartMix(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
	}
	
	@Test
	public void proxyMultipartForm_Success() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_FORM);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		mockResponse(HttpStatus.OK);
		
		service.proxyMultipartForm(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
	}
	
	@Test
	public void proxyMultipartHeader_Success() throws URISyntaxException {
		when(eccProperties.getMixContext()).thenReturn("/" + ProxyRequest.MULTIPART_HEADER);

		when(proxyRequest.getMessageType()).thenReturn(messageType);
		when(proxyRequest.getPayload()).thenReturn(PAYLOAD);
		
		when(restTemplate.exchange(any(URI.class), any(HttpMethod.class), 
				ArgumentMatchers.<HttpEntity<String>>any(), eq(String.class)))
			.thenReturn(response);
		mockResponse(HttpStatus.OK);
		
		service.proxyHttpHeader(proxyRequest, httpHeaders);
		
		verify(restTemplate).exchange(any(URI.class), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
	}
	
	private void mockResponse(HttpStatus status) {
		when(response.getStatusCode()).thenReturn(status);
		when(response.getHeaders()).thenReturn(httpHeaders);
		when(response.getBody()).thenReturn("Response Body");
	}
	
	private void mockMessageAsHeaders() {
		messageAsHeaders = new HashMap<>();
		messageAsHeaders.put("IDS-Messagetype", "ids:ArtifactRequestMessage");
		messageAsHeaders.put("IDS-Id", "https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f");
		messageAsHeaders.put("IDS-Issued", "2021-01-15T13:09:42.306Z");
		messageAsHeaders.put("IDS-IssuerConnector", "http://w3id.org/engrd/connector/");
		messageAsHeaders.put("IDS-ModelVersion", "4.0.0");
		messageAsHeaders.put("IDS-RequestedArtifact", "http://w3id.org/engrd/connector/artifact/1");
	}
	
	@Test
	public void parseIncommingProxyRequest() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getProxyRequest());
		assertNotNull(pr);
		assertEquals(ProxyRequest.MULTIPART_MIXED, pr.getMultipart());
		assertNotNull(pr.getPayload());
		assertNotNull(pr.getMessageType());
	}
	
	@Test
	public void parseIncommingProxyRequestWssRequestArtifact() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getProxyRequestRequestedArtifact());
		assertNotNull(pr);
		assertEquals(ProxyRequest.WSS, pr.getMultipart());
		assertEquals("test.csv", pr.getRequestedArtifact());
		assertNull(pr.getPayload());
		assertNull(pr.getMessageType());
	}
	
	@Test
	public void parseIncommingProxyRequestWssContractAgreement() {
		ProxyRequest pr = service.parseIncommingProxyRequest(getProxyRequestContractRequest());
		assertNotNull(pr);
		assertEquals(ProxyRequest.WSS, pr.getMultipart());
		assertNull(pr.getRequestedArtifact());
		assertNotNull(pr.getPayload());
		assertNotNull(pr.getMessageType());
	}

	private String getProxyRequest() {
		return "{\r\n" + 
				"   \"multipart\": \"mixed\",\r\n" + 
				"   \"messageType\": \"ArtifactRequestMessage\",\r\n" + 
				"	\"payload\" : {\r\n" + 
				"		\"catalog.offers.0.resourceEndpoints.path\":\"/pet2\"\r\n" + 
				"		},\r\n" + 
				"}";
	}
	
	private String getProxyRequestRequestedArtifact() {
		return "{\r\n" + 
				"    \"multipart\": \"wss\",\r\n" + 
				"    \"requestedArtifact\": \"test.csv\",\r\n" + 
				"}";
	}
	
	private String getProxyRequestContractRequest() {
		return "{\r\n" + 
				"    \"multipart\": \"wss\",\r\n" + 
				"    \"Forward-To\": \"wss://localhost:8086\",\r\n" + 
				"    \"Forward-To-Internal\": \"wss://localhost:8887\",\r\n" + 
				"    \"messageType\": \"ContractAgreementMessage\",\r\n" + 
				"\r\n" + 
				"    \"payload\": "
				+ 
				UtilMessageService.getMessageAsString(
						UtilMessageService.getContractAgreement()) 
				+"\r\n" + 
				"}\r\n";
	}

}

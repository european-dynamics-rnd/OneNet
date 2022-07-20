package it.eng.idsa.dataapp.service;

import java.net.URI;
import java.net.URISyntaxException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import it.eng.idsa.dataapp.domain.ProxyRequest;

public interface ProxyService {
	ResponseEntity<String> proxyMultipartMix(ProxyRequest proxyRequest, HttpHeaders httpHeaders) throws URISyntaxException;
	ResponseEntity<String> proxyMultipartForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders) throws URISyntaxException;
	ResponseEntity<String> proxyHttpHeader(ProxyRequest proxyRequest, HttpHeaders httpHeaders) throws URISyntaxException;
	ResponseEntity<String> proxyGetEntityForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders, String contextPath) throws URISyntaxException;
	ResponseEntity<String> proxyRegistrationEntityForm(ProxyRequest proxyRequest, HttpHeaders httpHeaders,
			String contextPath, URI myUri) throws URISyntaxException;
	
	ResponseEntity<String> requestArtifact(ProxyRequest proxyRequest);
	
	ProxyRequest parseIncommingProxyRequest(String body);
	ResponseEntity<String> proxyWSSRequest(ProxyRequest proxyRequest);
	
	ResponseEntity<String> convertToOrionResponse(ResponseEntity<String> resultEntity);
	ResponseEntity<String> convertToDataAppResponse(ResponseEntity<String> resultEntity);
	ResponseEntity<?> proxyCreationEntityForm(HttpHeaders httpHeaders, String body, URI myUri) throws URISyntaxException;
}

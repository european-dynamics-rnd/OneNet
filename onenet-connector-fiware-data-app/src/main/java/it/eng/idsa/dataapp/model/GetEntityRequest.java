package it.eng.idsa.dataapp.model;

import java.io.Serializable;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

/**
 * Class to wrap up original request, so it can be sent via Connectors, and recreated at other side
 * @author igor.balog
 *
 */
public class GetEntityRequest implements Serializable {

	private static final long serialVersionUID = -8199508846955607433L;

	private String originalPayload;
	private HttpMethod method;
	private HttpHeaders headers;
	private String requestPath;
	private HttpStatus statusCode;
	
	public GetEntityRequest() {}
	
	public GetEntityRequest(String originalPayload, HttpMethod method, HttpHeaders headers, String requestPath) {
		super();
		this.originalPayload = originalPayload;
		this.method = method;
		this.headers = headers;
		this.requestPath = requestPath;
	}
	
	/**
	 * Used in response flow, including status code
	 * @param originalPayload
	 * @param method
	 * @param headers
	 * @param requestPath
	 * @param statusCode
	 */
	public GetEntityRequest(String originalPayload, HttpMethod method, HttpHeaders headers, String requestPath,
			HttpStatus statusCode) {
		super();
		this.originalPayload = originalPayload;
		this.method = method;
		this.headers = headers;
		this.requestPath = requestPath;
		this.statusCode = statusCode;

	}

	public String getOriginalPayload() {
		return originalPayload;
	}
	public void setOriginalPayload(String originalPayload) {
		this.originalPayload = originalPayload;
	}
	public HttpMethod getMethod() {
		return method;
	}
	public void setMethod(HttpMethod method) {
		this.method = method;
	}
	public HttpHeaders getHeaders() {
		return headers;
	}
	public void setHeaders(HttpHeaders headers) {
		this.headers = headers;
	}
	public String getRequestPath() {
		return requestPath;
	}
	public void setRequestPath(String requestPath) {
		this.requestPath = requestPath;
	}
	public HttpStatus getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}

}

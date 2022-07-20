package it.eng.idsa.dataapp.model;

import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpStatusCodeException;

public class GetEntityException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	private HttpMethod method;
	private HttpStatusCodeException e;
	
	public GetEntityException(HttpStatusCodeException e, HttpMethod method) {
//		super(e);
		this.e = e;
		this.method = method;
	}
	
	public HttpMethod getMethod() {
		return method;
	}

	public HttpStatusCodeException getHttpStatusCodeException() {
		return e;
	}
}

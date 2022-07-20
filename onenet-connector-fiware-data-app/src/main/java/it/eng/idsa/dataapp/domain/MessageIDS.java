package it.eng.idsa.dataapp.domain;



/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public class MessageIDS {
	
	private String date;
	private String time;
	private String contentType;
	private String header;
	private String payload;
	
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getHeader() {
		return header;
	}
	public void setHeader(String header) {
		this.header = header;
	}
	public String getPayload() {
		return payload;
	}
	public void setPayload(String payload) {
		this.payload = payload;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
}

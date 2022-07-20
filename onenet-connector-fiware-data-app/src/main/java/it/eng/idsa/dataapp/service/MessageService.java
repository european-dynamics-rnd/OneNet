package it.eng.idsa.dataapp.service;

import java.util.List;

import it.eng.idsa.dataapp.domain.MessageIDS;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

public interface MessageService {
	
	public List<MessageIDS> getMessages();
	
	public void setMessage(String contentType, String header, String payload);
	
}

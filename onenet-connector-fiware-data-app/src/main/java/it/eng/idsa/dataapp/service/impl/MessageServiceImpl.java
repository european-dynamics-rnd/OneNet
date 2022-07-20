package it.eng.idsa.dataapp.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import it.eng.idsa.dataapp.domain.MessageIDS;
import it.eng.idsa.dataapp.service.MessageService;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Service
public class MessageServiceImpl implements MessageService {
	
	private List<MessageIDS> messages = new ArrayList<>();

	@Override
	public List<MessageIDS> getMessages() {
		return messages;
	}

	@Override
	public void setMessage(String contentType, String header, String payload) {
		MessageIDS messageIDS = new MessageIDS();
		messageIDS.setDate(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
		messageIDS.setTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
		messageIDS.setContentType(contentType);
		messageIDS.setHeader(header);
		messageIDS.setPayload(payload);
		messages.add(messageIDS);
	}

}

package it.eng.idsa.dataapp.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.eng.idsa.dataapp.model.OrionRequest;

public class OrionContextBrokerServiceTest {

	
	
	@Test
	public void serialiseTest() throws JsonProcessingException {
		OrionRequest oRequest = new OrionRequest("",HttpMethod.GET, null, "/ngs-ld/v1/entities/entity1");
		ObjectMapper mapper= new ObjectMapper();
		
		String s = mapper.writeValueAsString(oRequest);
		
		System.out.println(s);
		
		OrionRequest oRequest2 = mapper.readValue(s,OrionRequest.class);
		assertNotNull(oRequest2);
	}
}

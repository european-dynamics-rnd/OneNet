package it.eng.idsa.dataapp.service;

import org.springframework.http.ResponseEntity;

import it.eng.idsa.dataapp.model.OrionRequest;

public interface OrionContextBrokerService {

	ResponseEntity<String> enitityCall(OrionRequest orionRequest);
}

package it.eng.idsa.dataapp.service;

import org.springframework.http.ResponseEntity;

import it.eng.idsa.dataapp.model.GetEntityRequest;

public interface GetEntityService {

	ResponseEntity<String> entityCall(GetEntityRequest getEntityRequest);
	ResponseEntity<String> convertToDataAppResponse(ResponseEntity<String> resultEntity);

}

package it.eng.idsa.dataapp.service;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.Message;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing MultiPartMessage.
 */
public interface MultiPartMessageService {
	String getHeader(String body);
	String getPayload(String body);

	Message getMessage(String body);
	Message getMessage(Object header);
	String addToken(Message message, String token);
	HttpEntity createMultipartMessage(String header, String payload/*, String boundary, String contentType*/);
	String getToken(String message);
	String removeToken(Message message);
	String getResponseHeader(Message header);
	String getResponseHeader(String header);
	Message createRejectionMessageLocalIssues(Message header);
	Message createRejectionCommunicationLocalIssues(Message header);
	HttpEntity createMultipartMessageForm(String header, String payload, String frowardTo, ContentType ctPayload);
	Message createResultMessage(Message header);
	Message createArtifactResponseMessage(ArtifactRequestMessage header);
	Message createContractAgreementMessage(ContractRequestMessage header);

}

package it.eng.idsa.dataapp.service;

import java.io.File;
import java.io.IOException;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

/**
 * Service Interface for managing RecreateFileService.
 */
public interface RecreateFileService {
	
	/**
	 * Save file using default fileName and path
	 * @param payload
	 * @throws IOException
	 */
	void recreateTheFile(String payload) throws IOException;
	
	/**
	 * Save file with provided fileName and path
	 * @param payload
	 * @param targetFile
	 * @throws IOException
	 */
	void recreateTheFile(String payload, File targetFile) throws IOException;

}

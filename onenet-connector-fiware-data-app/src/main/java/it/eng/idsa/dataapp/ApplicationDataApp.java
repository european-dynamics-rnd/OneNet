package it.eng.idsa.dataapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@EnableCaching
@SpringBootApplication
public class ApplicationDataApp {

	public static void main(String[] args) {
		SpringApplication.run(ApplicationDataApp.class, args);
	}

}

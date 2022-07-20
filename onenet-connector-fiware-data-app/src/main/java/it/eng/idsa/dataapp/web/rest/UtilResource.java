package it.eng.idsa.dataapp.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@RestController
@EnableAutoConfiguration
@RequestMapping({ "/about" })
public class UtilResource {
	
	@Autowired
	BuildProperties buildProperties;
	
	@GetMapping("/version")
    @ResponseBody
    public String getVersion() {
//		return buildProperties.getVersion();
		return "1.0";
    }
	
}

package it.eng.idsa.dataapp.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 
 * @author Milan Karajovic and Gabriele De Luca
 *
 */

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).select().apis(RequestHandlerSelectors.any())
                .apis(RequestHandlerSelectors.basePackage("it.eng.idsa.dataapp.web.rest"))
				.paths(PathSelectors.any()).
				build();
	}

	@Bean
	public RestTemplate getRestTemplate() {
//		return new RestTemplate();
		// fix for Invalid HTTP method: PATCH
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		requestFactory.setReadTimeout(600000);
		requestFactory.setConnectTimeout(600000);
		return new RestTemplate(requestFactory);
	}
	
}

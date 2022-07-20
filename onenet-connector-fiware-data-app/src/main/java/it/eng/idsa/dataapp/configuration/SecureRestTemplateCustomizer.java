package it.eng.idsa.dataapp.configuration;

import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import it.eng.idsa.dataapp.web.rest.DataControllerBodyBinary;

@Component
@ConditionalOnProperty(name = "application.ecc.protocol", havingValue = "https")
public class SecureRestTemplateCustomizer implements RestTemplateCustomizer {

	private static final Logger logger = LoggerFactory.getLogger(DataControllerBodyBinary.class);

	@Value("${server.ssl.key-store}")
	private String trustStore;
	@Value("${server.ssl.key-password}")
	String trustStorePassword;

	String protocol = "TLSv1.2";

	@Override
	public void customize(RestTemplate restTemplate) {
		SSLContextBuilder sslcontextBuilder = SSLContexts.custom();
		HttpClient httpClient;
		try {
			sslcontextBuilder.loadTrustMaterial(null, (cert, auth) -> true);
			
			SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(
					sslcontextBuilder.build(), (HostnameVerifier) NoopHostnameVerifier.INSTANCE);
			httpClient = HttpClients.custom()
					.setSSLSocketFactory((LayeredConnectionSocketFactory) sslConnectionSocketFactory).build();
			final ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
			logger.info("Registered SSL truststore {} for client requests", trustStore);
			restTemplate.setRequestFactory(requestFactory);
		} catch (Exception e) {
			throw new IllegalStateException("Failed to setup client SSL context", e);
		} finally {
			// it's good security practice to zero out passwords,
			// which is why they're char[]
			Arrays.fill(trustStorePassword.toCharArray(), (char) 0);
		}
	}
}

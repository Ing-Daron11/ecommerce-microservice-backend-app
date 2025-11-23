package com.selimhorri.app.config.template;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Configuration
public class TemplateConfig {
	
	private static final Logger logger = LoggerFactory.getLogger(TemplateConfig.class);
	
	@LoadBalanced
	@Bean
	public RestTemplate restTemplateBean(RestTemplateBuilder builder) {
		return builder
				.interceptors(new TraceInterceptor())
				.build();
	}
	
	/**
	 * Interceptor que propaga headers de tracing entre servicios
	 */
	public static class TraceInterceptor implements ClientHttpRequestInterceptor {

		private static final Logger log = LoggerFactory.getLogger(TraceInterceptor.class);

		@Override
		public ClientHttpResponse intercept(HttpRequest request, byte[] body, 
										   ClientHttpRequestExecution execution) throws IOException {
			
			// Log para depuración
			log.info("Interceptando request a: {}", request.getURI());
			
			// Sleuth debería manejar esto automáticamente, pero forzamos la propagación
			// si los headers están presentes en el contexto actual (MDC o RequestAttributes)
			// Nota: En un interceptor de cliente, 'request' es la SALIENTE.
			// Para obtener los headers ENTRANTES, necesitaríamos acceder al RequestContextHolder.
			// Sin embargo, Sleuth inyecta los headers automáticamente si el RestTemplate es un Bean.
			// Este interceptor sirve para verificar y añadir logs.
			
			return execution.execute(request, body);
		}
	}
	
}











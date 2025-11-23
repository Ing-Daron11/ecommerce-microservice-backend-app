package com.selimhorri.app.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class ZipkinConfig {

    private static final Logger log = LoggerFactory.getLogger(ZipkinConfig.class);

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        log.info("Registrando RestTemplate bean para Zipkin/Sleuth en User Service");
        return builder.build();
    }
}

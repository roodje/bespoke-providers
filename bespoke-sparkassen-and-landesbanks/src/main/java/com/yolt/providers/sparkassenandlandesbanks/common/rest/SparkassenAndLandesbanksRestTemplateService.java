package com.yolt.providers.sparkassenandlandesbanks.common.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.sparkassenandlandesbanks.common.SparkassenAndLandesbanksAuthMeans;
import com.yolt.providers.sparkassenandlandesbanks.common.config.SparkassenAndLandesbanksProperties;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.web.ProjectingJackson2HttpMessageConverter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
public class SparkassenAndLandesbanksRestTemplateService {

    private final MeterRegistry registry;
    private final ObjectMapper objectMapper;
    private final SparkassenAndLandesbanksProperties properties;
    private final Clock clock;

    private static final String EXTERNAL_TRACE_ID_HEADER_NAME = "X-Request-ID";

    public SparkassenAndLandesbanksHttpClient createHttpClient(SparkassenAndLandesbanksAuthMeans authenticationMeans,
                                                               RestTemplateManager restTemplateManager,
                                                               String provider) {

        Jaxb2RootElementHttpMessageConverter jaxbMessageConverter = new Jaxb2RootElementHttpMessageConverter();
        jaxbMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_XML));

        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getClientTransportCertificate(),
                externalRestTemplateBuilderFactory -> externalRestTemplateBuilderFactory
                        .messageConverters(
                                new ProjectingJackson2HttpMessageConverter(objectMapper),
                                new MappingJackson2HttpMessageConverter(objectMapper),
                                new FormHttpMessageConverter(),
                                jaxbMessageConverter)
                        .externalTracing(EXTERNAL_TRACE_ID_HEADER_NAME)
                        .rootUri(properties.getBaseUrl())
                        .build())
        );

        return new SparkassenAndLandesbanksHttpClient(registry, restTemplate, provider, clock);
    }
}
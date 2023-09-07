package com.yolt.providers.redsys.evo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.RestTemplateManagerConfiguration;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import com.yolt.providers.redsys.common.rest.RedsysHttpClient;
import com.yolt.providers.redsys.common.rest.RedsysRestTemplateService;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.time.Clock;

public class EvoRestTemplateService extends RedsysRestTemplateService {

    protected final ObjectMapper objectMapper;
    protected final RedsysBaseProperties properties;
    private final Clock clock;

    public EvoRestTemplateService(final ObjectMapper objectMapper, final RedsysBaseProperties properties, final Clock clock) {
        super(objectMapper, properties);
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.clock = clock;
    }

    public RedsysHttpClient createHttpClient(final RedsysAuthenticationMeans authenticationMeans,
                                             final RestTemplateManager restTemplateManager) {
        RestTemplate restTemplate = restTemplateManager.manage(new RestTemplateManagerConfiguration(
                authenticationMeans.getTransportKeyId(),
                authenticationMeans.getTransportCertificate(),
                externalRestTemplateBuilderFactory ->
                        externalRestTemplateBuilderFactory
                                .rootUri(properties.getBaseUrl())
                                .messageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                                .build())
        );
        return new EvoHttpClient(restTemplate, clock);
    }

}

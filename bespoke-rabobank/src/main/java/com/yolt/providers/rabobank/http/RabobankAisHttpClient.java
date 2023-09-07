package com.yolt.providers.rabobank.http;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.DefaultHttpClient;
import com.yolt.providers.common.rest.http.ProviderClientEndpoints;
import com.yolt.providers.rabobank.config.RabobankProperties;
import com.yolt.providers.rabobank.dto.AccessTokenResponseDTO;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class RabobankAisHttpClient extends DefaultHttpClient {

    private static final String TOKEN_ENDPOINT = "oauth2/token";
    private final RabobankProperties properties;

    public RabobankAisHttpClient(MeterRegistry registry, RestTemplate restTemplate, String provider, RabobankProperties properties) {
        super(registry, restTemplate, provider);
        this.properties = properties;
    }

    public AccessTokenResponseDTO refreshAccessMeans(HttpEntity<MultiValueMap<String, String>> requestEntity) throws TokenInvalidException {
        return exchange(properties.getBaseAuthorizationUrl() + TOKEN_ENDPOINT,
                HttpMethod.POST,
                requestEntity,
                ProviderClientEndpoints.REFRESH_TOKEN,
                AccessTokenResponseDTO.class
        ).getBody();
    }
}

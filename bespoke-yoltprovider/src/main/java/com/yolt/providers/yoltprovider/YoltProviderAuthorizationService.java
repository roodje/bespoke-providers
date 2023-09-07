package com.yolt.providers.yoltprovider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

public class YoltProviderAuthorizationService {
    private static final String EXCHANGE_URL_PART = "/authorize/exchange";

    public String exchangeAuthorizationCodeForAccessToken(final RestTemplate restTemplate, final String authorizationCode) {
        final Token token = restTemplate.postForObject(EXCHANGE_URL_PART, new Code(authorizationCode), Token.class);
        return Objects.requireNonNull(token).getAccessToken();
    }

    @Data
    @AllArgsConstructor
    private static class Code {
        private String code; //NOSONAR
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Token {
        @JsonProperty("access_token")
        private String accessToken;
    }
}

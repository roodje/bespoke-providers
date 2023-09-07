package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.domain.Scope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StetTokenPaymentHttpRequestBodyProviderTest {

    private static final Scope SCOPE = Scope.PISP;

    private StetTokenPaymentHttpRequestBodyProvider tokenPaymentHttpRequestBodyProvider;

    @BeforeEach
    void initialize() {
        tokenPaymentHttpRequestBodyProvider = new StetTokenPaymentHttpRequestBodyProvider(SCOPE);
    }

    @Test
    void shouldCreateRequestBody() {
        // when
        MultiValueMap<String, String> requestBody = tokenPaymentHttpRequestBodyProvider.createRequestBody(null);

        // then
        assertThat(requestBody)
                .hasSize(2)
                .containsEntry(OAuth.GRANT_TYPE, List.of(OAuth.CLIENT_CREDENTIALS))
                .containsEntry(OAuth.SCOPE, List.of(SCOPE.getValue()));
    }

    @Test
    void shouldCreateHttpEntity() {
        // given
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add(OAuth.GRANT_TYPE, OAuth.CLIENT_CREDENTIALS);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        // when
        HttpEntity<Map<String, ?>> httpEntity = tokenPaymentHttpRequestBodyProvider.createHttpEntity(requestBody, httpHeaders);

        // then
    }
}

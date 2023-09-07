package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientFactoryV2;
import com.yolt.providers.volksbank.common.rest.VolksbankPisHttpClientV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class VolksbankSubmitPaymentHttpRequestInvokerV2Test {

    private VolksbankSubmitPaymentHttpRequestInvokerV2 subject;

    @Mock
    private VolksbankHttpClientFactoryV2 httpClientFactory;

    @Mock
    private HttpEntity<Void> httpEntity;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private VolksbankPisHttpClientV2 pisHttpClient;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;

    @BeforeEach
    void beforeEach() {
        subject = new VolksbankSubmitPaymentHttpRequestInvokerV2(httpClientFactory,
                new ProviderIdentification("VOLKSBANK",
                        "Volksbank",
                        ProviderVersion.VERSION_1));
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestWhenCorrectData() throws TokenInvalidException {
        // given
        var authenticationMeans = VolksbankAuthenticationMeans.fromAuthenticationMeans(new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans(), "VOLKSBANK");
        var preExecutionResult = new VolksbankSepaSubmitPreExecutionResult(
                authenticationMeans,
                restTemplateManager,
                "fakePaymentId"
        );

        given(httpClientFactory.createPisHttpClient(any(VolksbankAuthenticationMeans.class), any(RestTemplateManager.class), anyString()))
                .willReturn(pisHttpClient);
        given(pisHttpClient.getPaymentStatus(any(HttpEntity.class), anyString()))
                .willReturn(responseEntity);

        // when
        var result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        then(httpClientFactory)
                .should()
                .createPisHttpClient(authenticationMeans, restTemplateManager, "Volksbank");
        then(pisHttpClient)
                .should()
                .getPaymentStatus(httpEntity, "fakePaymentId");
        assertThat(result).isEqualTo(responseEntity);
    }
}
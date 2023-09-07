package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import com.yolt.providers.volksbank.common.rest.VolksbankHttpClientFactoryV2;
import com.yolt.providers.volksbank.common.rest.VolksbankPisHttpClientV2;
import com.yolt.providers.volksbank.dto.v1_1.InitiatePaymentRequest;
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
class VolksbankInitiatePaymentHttpRequestInvokerV2Test {

    private VolksbankInitiatePaymentHttpRequestInvokerV2 subject;

    @Mock
    private VolksbankHttpClientFactoryV2 httpClientFactory;

    @Mock
    private VolksbankPisHttpClientV2 pisHttpClient;

    @Mock
    private HttpEntity<InitiatePaymentRequest> httpEntity;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;

    @Mock
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    void beforeEach() {
        subject = new VolksbankInitiatePaymentHttpRequestInvokerV2(httpClientFactory,
                new ProviderIdentification("VOLKSBANK",
                        "Volksbank",
                        ProviderVersion.VERSION_1));
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestWhenCorrectData() throws TokenInvalidException {
        // given
        var authenticationMeans = prepareAuthMeans();
        var preExecutionResult = preparePreExecutionResult(authenticationMeans);

        given(httpClientFactory.createPisHttpClient(any(VolksbankAuthenticationMeans.class), any(RestTemplateManager.class), anyString()))
                .willReturn(pisHttpClient);
        given(pisHttpClient.initiatePayment(any(HttpEntity.class)))
                .willReturn(responseEntity);

        // when
        var result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        then(httpClientFactory)
                .should()
                .createPisHttpClient(authenticationMeans, restTemplateManager, "Volksbank");
        then(pisHttpClient)
                .should()
                .initiatePayment(httpEntity);
        assertThat(result).isEqualTo(responseEntity);
    }

    private VolksbankAuthenticationMeans prepareAuthMeans() {
        return VolksbankAuthenticationMeans
                .fromAuthenticationMeans(new VolksbankSampleTypedAuthenticationMeans()
                                .getAuthenticationMeans(),
                        "VOLKSBANK");
    }

    private VolksbankSepaInitiatePreExecutionResult preparePreExecutionResult(VolksbankAuthenticationMeans authenticationMeans) {
        return new VolksbankSepaInitiatePreExecutionResult(
                null,
                authenticationMeans,
                restTemplateManager,
                "",
                "",
                ""
        );
    }
}
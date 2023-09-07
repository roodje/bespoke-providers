package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.model.InitiatePaymentRequest;
import com.yolt.providers.cbiglobe.common.rest.CbiGlobePisHttpClientFactory;
import com.yolt.providers.cbiglobe.common.rest.CbiGlobePisHttpClient;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CbiGlobeInitiatePaymentHttpRequestInvokerTest {

    private CbiGlobeInitiatePaymentHttpRequestInvoker subject;

    @Mock
    private CbiGlobePisHttpClientFactory httpClientFactory;

    @Mock
    private CbiGlobePisHttpClient pisHttpClient;

    @Mock
    private HttpEntity<InitiatePaymentRequest> httpEntity;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;

    @Mock
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    void beforeEach() {
        subject = new CbiGlobeInitiatePaymentHttpRequestInvoker(httpClientFactory,
                new ProviderIdentification("CbiGlobe",
                        "CbiGlobe",
                        ProviderVersion.VERSION_1));
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestWhenCorrectData() throws TokenInvalidException, IOException, URISyntaxException {
        // given
        var authenticationMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(
                new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans(), "CBI_GLOBE");
        var preExecutionResult = preparePreExecutionResult(authenticationMeans);

        given(httpClientFactory.createPisHttpClient(any(CbiGlobeAuthenticationMeans.class), any(RestTemplateManager.class), anyString()))
                .willReturn(pisHttpClient);
        given(pisHttpClient.initiatePayment(any(HttpEntity.class)))
                .willReturn(responseEntity);

        // when
        var result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        then(httpClientFactory)
                .should()
                .createPisHttpClient(authenticationMeans, restTemplateManager, "CbiGlobe");
        then(pisHttpClient)
                .should()
                .initiatePayment(httpEntity);
        assertThat(result).isEqualTo(responseEntity);
    }

    private CbiGlobeSepaInitiatePreExecutionResult preparePreExecutionResult(CbiGlobeAuthenticationMeans authenticationMeans) {
        return new CbiGlobeSepaInitiatePreExecutionResult(
                null,
                authenticationMeans,
                restTemplateManager,
                "",
                "",
                null,
                null,
                null
        );
    }
}
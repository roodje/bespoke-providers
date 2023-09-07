package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CbiGlobeSubmitPaymentHttpRequestInvokerTest {

    private CbiGlobeSubmitPaymentHttpRequestInvoker subject;

    @Mock
    private CbiGlobePisHttpClientFactory httpClientFactory;

    @Mock
    private HttpEntity<Void> httpEntity;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private CbiGlobePisHttpClient pisHttpClient;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;

    @BeforeEach
    void beforeEach() {
        subject = new CbiGlobeSubmitPaymentHttpRequestInvoker(httpClientFactory,
                new ProviderIdentification("CbiGlobe",
                        "CbiGlobe",
                        ProviderVersion.VERSION_1));
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyForInvokeRequestWhenCorrectData() throws TokenInvalidException {
        // given
        var authenticationMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(
                new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans(),
                "CBI_GLOBE");
        var preExecutionResult = new CbiGlobeSepaSubmitPreExecutionResult(
                authenticationMeans,
                restTemplateManager,
                "fakePaymentId",
                null,
                null,
                null
        );

        given(httpClientFactory.createPisHttpClient(any(CbiGlobeAuthenticationMeans.class), any(RestTemplateManager.class), anyString()))
                .willReturn(pisHttpClient);
        given(pisHttpClient.getPaymentStatus(any(HttpEntity.class), anyString()))
                .willReturn(responseEntity);

        // when
        var result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        then(httpClientFactory)
                .should()
                .createPisHttpClient(authenticationMeans, restTemplateManager, "CbiGlobe");
        then(pisHttpClient)
                .should()
                .getPaymentStatus(httpEntity, "fakePaymentId");
        assertThat(result).isEqualTo(responseEntity);
    }
}
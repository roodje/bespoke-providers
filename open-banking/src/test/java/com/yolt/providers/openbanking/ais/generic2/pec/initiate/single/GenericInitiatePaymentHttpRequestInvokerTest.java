package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.fasterxml.jackson.databind.JsonNode;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.EndpointsVersionable;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.generic2.pec.common.exception.GenericPaymentRequestInvocationException;
import com.yolt.providers.openbanking.ais.generic2.pec.restclient.PisRestClient;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsent4;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericInitiatePaymentHttpRequestInvokerTest {

    private GenericInitiatePaymentHttpRequestInvoker subject;

    @Mock
    private PisRestClient pisRestClient;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpEntity<OBWriteDomesticConsent4> httpEntity;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;

    @Mock
    private EndpointsVersionable endpointsVersionable;

    @BeforeEach
    void beforeEach() {
        subject = new GenericInitiatePaymentHttpRequestInvoker(pisRestClient,
                httpClientFactory,
                endpointsVersionable,
                new ProviderIdentification("PROVIDER",
                        "Provider",
                        ProviderVersion.VERSION_1));
    }

    @Test
    void shouldReturnResponseEntityWithJsonNodeAsBodyWhenCorrectDataAreProvided() throws TokenInvalidException {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericInitiatePaymentPreExecutionResult preExecutionResult = new GenericInitiatePaymentPreExecutionResult();
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setRestTemplateManager(restTemplateManager);

        given(httpClientFactory.createHttpClient(any(RestTemplateManager.class), any(DefaultAuthMeans.class), anyString()))
                .willReturn(httpClient);
        given(pisRestClient.createPayment(any(HttpClient.class), anyString(), any(HttpEntity.class)))
                .willReturn(responseEntity);
        given(endpointsVersionable.getAdjustedUrlPath(anyString()))
                .willReturn("adjustedVersionAwareUrl");

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        then(httpClientFactory)
                .should()
                .createHttpClient(restTemplateManager, authMeans, "Provider");
        then(pisRestClient)
                .should()
                .createPayment(httpClient, "adjustedVersionAwareUrl", httpEntity);
        then(endpointsVersionable)
                .should()
                .getAdjustedUrlPath("/pisp/domestic-payment-consents");

        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldThrowGenericPaymentRequestInvocationExceptionWhenTokenInvalidExceptionIsThrownByHttpClient() throws TokenInvalidException {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericInitiatePaymentPreExecutionResult preExecutionResult = new GenericInitiatePaymentPreExecutionResult();
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setRestTemplateManager(restTemplateManager);

        given(httpClientFactory.createHttpClient(any(RestTemplateManager.class), any(DefaultAuthMeans.class), anyString()))
                .willReturn(httpClient);
        given(pisRestClient.createPayment(any(HttpClient.class), anyString(), any(HttpEntity.class)))
                .willThrow(TokenInvalidException.class);
        given(endpointsVersionable.getAdjustedUrlPath(anyString()))
                .willReturn("adjustedVersionAwareUrl");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThatExceptionOfType(GenericPaymentRequestInvocationException.class)
                .isThrownBy(callable)
                .withCauseInstanceOf(TokenInvalidException.class);
    }
}
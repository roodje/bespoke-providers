package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

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
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsent4;
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

@ExtendWith(MockitoExtension.class)
class GenericInitiateScheduledPaymentHttpRequestInvokerTest {

    private GenericInitiateScheduledPaymentHttpRequestInvoker subject;

    @Mock
    private PisRestClient pisRestClient;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private HttpClient httpClient;

    @Mock
    private HttpEntity<OBWriteDomesticScheduledConsent4> httpEntity;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;

    @Mock
    private EndpointsVersionable endpointsVersionable;

    @BeforeEach
    void beforeEach() {
        subject = new GenericInitiateScheduledPaymentHttpRequestInvoker(pisRestClient,
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
        GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult = new GenericInitiateScheduledPaymentPreExecutionResult();
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setRestTemplateManager(restTemplateManager);

        given(httpClientFactory.createHttpClient(restTemplateManager, authMeans, "Provider"))
                .willReturn(httpClient);
        given(pisRestClient.createPayment(httpClient, "adjustedVersionAwareUrl", httpEntity))
                .willReturn(responseEntity);
        given(endpointsVersionable.getAdjustedUrlPath("/pisp/domestic-scheduled-payment-consents"))
                .willReturn("adjustedVersionAwareUrl");

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldThrowGenericPaymentRequestInvocationExceptionWhenTokenInvalidExceptionIsThrownByHttpClient() throws TokenInvalidException {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult = new GenericInitiateScheduledPaymentPreExecutionResult();
        preExecutionResult.setAuthMeans(authMeans);
        preExecutionResult.setRestTemplateManager(restTemplateManager);
        Exception expectedException = new TokenInvalidException("Expired token");
        given(httpClientFactory.createHttpClient(any(RestTemplateManager.class), any(DefaultAuthMeans.class), anyString()))
                .willReturn(httpClient);
        given(pisRestClient.createPayment(any(HttpClient.class), anyString(), any(HttpEntity.class)))
                .willThrow(expectedException);
        given(endpointsVersionable.getAdjustedUrlPath(anyString()))
                .willReturn("adjustedVersionAwareUrl");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThatExceptionOfType(GenericPaymentRequestInvocationException.class)
                .isThrownBy(callable)
                .withCause(expectedException);
    }
}
package com.yolt.providers.openbanking.ais.generic2.pec.status.scheduled;

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
import com.yolt.providers.openbanking.ais.generic2.pec.status.single.GenericPaymentStatusPreExecutionResult;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GenericScheduledPaymentStatusHttpRequestInvokerTest {

    private GenericScheduledPaymentStatusHttpRequestInvoker subject;

    @Mock
    private HttpClientFactory httpClientFactory;

    @Mock
    private PisRestClient pisRestClient;

    @Mock
    private ResponseEntity<JsonNode> responseEntity;

    @Mock
    private HttpEntity<Void> httpEntity;

    @Mock
    private HttpClient httpClient;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private EndpointsVersionable endpointsVersionable;

    @BeforeEach
    void beforeEach() {
        subject = new GenericScheduledPaymentStatusHttpRequestInvoker(httpClientFactory,
                pisRestClient,
                endpointsVersionable,
                new ProviderIdentification("PROVIDER",
                        "Provider",
                        ProviderVersion.VERSION_1));
    }

    @Test
    void shouldCallGetPaymentStatusAndReturnResponseEntityWithJsonNodeAsBodyWhenOnlyPaymentIdProvidedInPreExecutionResult() throws TokenInvalidException {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericPaymentStatusPreExecutionResult preExecutionResult = createGenericStatusPaymentPreExecutionResult(authMeans, "paymentId", null);

        given(httpClientFactory.createHttpClient(restTemplateManager, authMeans, "Provider"))
                .willReturn(httpClient);
        given(pisRestClient.getPaymentStatus(httpClient, "adjustedVersionAwareUrl", httpEntity))
                .willReturn(responseEntity);
        given(endpointsVersionable.getAdjustedUrlPath("/pisp/domestic-scheduled-payments/paymentId"))
                .willReturn("adjustedVersionAwareUrl");

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, preExecutionResult);
        //then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldCallGetConsentStatusAndReturnResponseEntityWithJsonNodeAsBodyWhenOnlyConsentIdProvidedInPreExecutionResult() throws TokenInvalidException {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericPaymentStatusPreExecutionResult preExecutionResult = createGenericStatusPaymentPreExecutionResult(authMeans, null, "consentId");

        given(httpClientFactory.createHttpClient(restTemplateManager, authMeans, "Provider"))
                .willReturn(httpClient);
        given(pisRestClient.getConsentStatus(httpClient, "adjustedVersionAwareUrl", httpEntity))
                .willReturn(responseEntity);
        given(endpointsVersionable.getAdjustedUrlPath("/pisp/domestic-scheduled-payment-consents/consentId"))
                .willReturn("adjustedVersionAwareUrl");

        // when
        ResponseEntity<JsonNode> result = subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThat(result).isEqualTo(responseEntity);
    }

    @Test
    void shouldFavorPaymentIdOverConsentIdAndCallGetPaymentStatusAndReturnResponseEntityWithJsonNodeAsBodyWhenBothPaymentIdAndConsentIdAreProvidedInPreExecutionResult() throws TokenInvalidException {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericPaymentStatusPreExecutionResult preExecutionResult = createGenericStatusPaymentPreExecutionResult(authMeans, "paymentId", "consentId");

        given(httpClientFactory.createHttpClient(restTemplateManager, authMeans, "Provider"))
                .willReturn(httpClient);
        given(pisRestClient.getPaymentStatus(httpClient, "adjustedVersionAwareUrl", httpEntity))
                .willReturn(responseEntity);
        given(endpointsVersionable.getAdjustedUrlPath("/pisp/domestic-scheduled-payments/paymentId"))
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
        GenericPaymentStatusPreExecutionResult preExecutionResult = createGenericStatusPaymentPreExecutionResult(authMeans, "paymentId", null);
        Exception expectedException = new TokenInvalidException("Expired token");

        given(httpClientFactory.createHttpClient(restTemplateManager, authMeans, "Provider"))
                .willReturn(httpClient);
        given(pisRestClient.getPaymentStatus(httpClient, "adjustedVersionAwareUrl", httpEntity))
                .willThrow(expectedException);
        given(endpointsVersionable.getAdjustedUrlPath("/pisp/domestic-scheduled-payments/paymentId"))
                .willReturn("adjustedVersionAwareUrl");

        // when
        ThrowableAssert.ThrowingCallable callable = () -> subject.invokeRequest(httpEntity, preExecutionResult);

        // then
        assertThatExceptionOfType(GenericPaymentRequestInvocationException.class)
                .isThrownBy(callable)
                .withCause(expectedException);
    }

    private GenericPaymentStatusPreExecutionResult createGenericStatusPaymentPreExecutionResult(DefaultAuthMeans authMeans,
                                                                                                String paymentId,
                                                                                                String consentId) {
        return new GenericPaymentStatusPreExecutionResult(null, authMeans, restTemplateManager, paymentId, consentId);
    }
}
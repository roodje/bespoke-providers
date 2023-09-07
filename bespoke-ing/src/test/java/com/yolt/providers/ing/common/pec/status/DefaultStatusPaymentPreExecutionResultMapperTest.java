package com.yolt.providers.ing.common.pec.status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.GetStatusRequestBuilder;
import com.yolt.providers.ing.common.IngSampleAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.dto.PaymentProviderState;
import com.yolt.providers.ing.common.exception.ProviderStateDeserializationException;
import com.yolt.providers.ing.common.pec.DefaultPisAccessMeansProvider;
import com.yolt.providers.ing.common.pec.submit.DefaultSubmitPaymentPreExecutionResult;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class DefaultStatusPaymentPreExecutionResultMapperTest {

    @InjectMocks
    private DefaultStatusPaymentPreExecutionResultMapper sut;

    @Mock
    private DefaultPisAccessMeansProvider accessMeansProvider;

    @Mock
    private Signer signer;

    @Mock
    private IngClientAccessMeans accessMeans;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Clock clock;

    @Test
    void shouldReturnPreExecutionResultWithPaymentIdTakenFromRequestDtoWhenPaymentIdIsProvidedInRequest() throws IOException, URISyntaxException {
        // given
        var authMeans = (new IngSampleAuthenticationMeans()).getAuthenticationMeans();
        var request = createGetStatusRequest(authMeans, true);

        given(accessMeansProvider.getClientAccessMeans(any(IngAuthenticationMeans.class), any(RestTemplateManager.class), any(Signer.class), any(Clock.class)))
                .willReturn(accessMeans);

        // when
        var result = sut.map(request);

        // then
        assertThat(result.getAuthenticationMeans()).isNotNull();
        assertThat(result.getClientAccessMeans()).isNotNull();
        assertThat(result).extracting(
                DefaultSubmitPaymentPreExecutionResult::getPaymentId,
                DefaultSubmitPaymentPreExecutionResult::getRestTemplateManager,
                DefaultSubmitPaymentPreExecutionResult::getPsuIpAddress,
                DefaultSubmitPaymentPreExecutionResult::getSigner
        ).contains("testPaymentId", restTemplateManager, "fakePsuIpAddress", signer);
    }

    @Test
    void shouldReturnPreExecutionResultWithPaymentIdTakenFromProviderStateWhenPaymentIdIsNotProvidedInRequest() throws IOException, URISyntaxException {
        // given
        var authMeans = (new IngSampleAuthenticationMeans()).getAuthenticationMeans();
        var request = createGetStatusRequest(authMeans, false);

        given(objectMapper.readValue(anyString(), eq(PaymentProviderState.class)))
                .willReturn(new PaymentProviderState("testPaymentIdFromState", null));
        given(accessMeansProvider.getClientAccessMeans(any(IngAuthenticationMeans.class), any(RestTemplateManager.class), any(Signer.class), any(Clock.class)))
                .willReturn(accessMeans);

        // when
        var result = sut.map(request);

        // then
        then(objectMapper)
                .should()
                .readValue("providerState", PaymentProviderState.class);

        assertThat(result.getAuthenticationMeans()).isNotNull();
        assertThat(result.getClientAccessMeans()).isNotNull();
        assertThat(result).extracting(
                DefaultSubmitPaymentPreExecutionResult::getPaymentId,
                DefaultSubmitPaymentPreExecutionResult::getRestTemplateManager,
                DefaultSubmitPaymentPreExecutionResult::getPsuIpAddress,
                DefaultSubmitPaymentPreExecutionResult::getSigner
        ).contains("testPaymentIdFromState", restTemplateManager, "fakePsuIpAddress", signer);
    }

    @Test
    void shouldThrowProviderStateDeserializationExceptionWhenJsonProcessingExceptionIsThrownDuringProviderStateDeserialization() throws IOException, URISyntaxException {
        // given
        var authMeans = (new IngSampleAuthenticationMeans()).getAuthenticationMeans();
        var request = createGetStatusRequest(authMeans, false);

        given(objectMapper.readValue(anyString(), eq(PaymentProviderState.class)))
                .willThrow(JsonProcessingException.class);

        // when
        ThrowableAssert.ThrowingCallable callable = () -> sut.map(request);

        // then
        assertThatExceptionOfType(ProviderStateDeserializationException.class)
                .isThrownBy(callable)
                .withMessage("Cannot deserialize provider state")
                .withCauseInstanceOf(JsonProcessingException.class);
    }

    private GetStatusRequest createGetStatusRequest(Map<String, BasicAuthenticationMean> authMeans, boolean withPaymentId) {
        return new GetStatusRequestBuilder()
                .setPaymentId(withPaymentId ? "testPaymentId" : null)
                .setProviderState(withPaymentId ? null : "providerState")
                .setSigner(signer)
                .setAuthenticationMeans(authMeans)
                .setRestTemplateManager(restTemplateManager)
                .setPsuIpAddress("fakePsuIpAddress")
                .build();
    }
}
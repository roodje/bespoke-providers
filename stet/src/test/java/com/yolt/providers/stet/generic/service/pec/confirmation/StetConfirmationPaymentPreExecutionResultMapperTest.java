package com.yolt.providers.stet.generic.service.pec.confirmation;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.PaymentProviderState;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.status.StetStatusPaymentPreExecutionResultMapper;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class StetConfirmationPaymentPreExecutionResultMapperTest {

    private static final String PAYMENT_ID = "4523782332";
    private static final String SERIALIZED_PROVIDER_STATE = String.format("{\"paymentId\":\"%s\"}", PAYMENT_ID);
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String REDIRECT_URL_POSTED_BACK_FROM_BANK = String.format("https://stetbank.com/payment/%s", PAYMENT_ID);
    private static final String ACCESS_TOKEN = "cdbcd575-046a-4035-916a-85efc92d63ef";

    @Mock
    private StetStatusPaymentPreExecutionResultMapper<StetTokenPaymentPreExecutionResult> statusPaymentPreExecutionResultMapper;

    @Mock
    private ProviderStateMapper providerStateMapper;

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    @Captor
    private ArgumentCaptor<GetStatusRequest> getStatusRequestCaptor;

    private StetConfirmationPaymentPreExecutionResultMapper<StetTokenPaymentPreExecutionResult> confirmationPaymentPreExecutionResultMapper;

    @BeforeEach
    void initialize() {
        confirmationPaymentPreExecutionResultMapper = new StetConfirmationPaymentPreExecutionResultMapper<>(
                statusPaymentPreExecutionResultMapper,
                providerStateMapper);
    }

    @Test
    void shouldMapToStetConfirmationPreExecutionResult() {
        // given
        Map<String, BasicAuthenticationMean> basicAuthenticationMeans = new HashMap<>();
        AuthenticationMeansReference authenticationMeansReference = new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID());
        SubmitPaymentRequest request = new SubmitPaymentRequest(
                SERIALIZED_PROVIDER_STATE,
                new HashMap<>(),
                REDIRECT_URL_POSTED_BACK_FROM_BANK,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                authenticationMeansReference);

        StetConfirmationPreExecutionResult expectedPreExecutionResult = StetConfirmationPreExecutionResult.builder()
                .paymentId(PAYMENT_ID)
                .httpMethod(HttpMethod.GET)
                .accessToken(ACCESS_TOKEN)
                .authMeans(authenticationMeans)
                .psuIpAddress(PSU_IP_ADDRESS)
                .build();

        given(providerStateMapper.mapToPaymentProviderState(anyString()))
                .willReturn(PaymentProviderState.initiatedProviderState(PAYMENT_ID));
        given(statusPaymentPreExecutionResultMapper.map(any(GetStatusRequest.class)))
                .willReturn(expectedPreExecutionResult);

        // when
        StetConfirmationPreExecutionResult preExecutionResult = confirmationPaymentPreExecutionResultMapper.map(request);

        // then
        assertThat(preExecutionResult).isEqualTo(expectedPreExecutionResult);

        then(providerStateMapper)
                .should()
                .mapToPaymentProviderState(SERIALIZED_PROVIDER_STATE);
        then(statusPaymentPreExecutionResultMapper)
                .should()
                .map(getStatusRequestCaptor.capture());

        GetStatusRequest getStatusRequest = getStatusRequestCaptor.getValue();
        assertThat(getStatusRequest.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(getStatusRequest.getProviderState()).isEqualTo(SERIALIZED_PROVIDER_STATE);
        assertThat(getStatusRequest.getPsuIpAddress()).isEqualTo(PSU_IP_ADDRESS);
        assertThat(getStatusRequest.getSigner()).isEqualTo(signer);
        assertThat(getStatusRequest.getRestTemplateManager()).isEqualTo(restTemplateManager);
        assertThat(getStatusRequest.getAuthenticationMeans()).isEqualTo(basicAuthenticationMeans);
        assertThat(getStatusRequest.getAuthenticationMeansReference()).isEqualTo(authenticationMeansReference);
    }
}

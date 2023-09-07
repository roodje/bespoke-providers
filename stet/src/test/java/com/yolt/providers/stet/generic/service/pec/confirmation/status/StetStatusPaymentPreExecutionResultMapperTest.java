package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.ProviderIdentification;
import com.yolt.providers.stet.generic.dto.token.TokenResponseDTO;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentHttpRequestInvoker;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResultMapper;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import nl.ing.lovebird.providershared.api.AuthenticationMeansReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@Deprecated
public class StetStatusPaymentPreExecutionResultMapperTest {

    private static final String PROVIDER_IDENTIFIER = "STET_PROVIDER";
    private static final String PROVIDER_DISPLAY_NAME = "Stet Provider";
    private static final String PAYMENT_ID = "4523782332";
    private static final String PROVIDER_STATE = String.format("{\"paymentId\":\"%s\"}", PAYMENT_ID);
    private static final String PSU_IP_ADDRESS = "127.0.0.1";
    private static final String ACCESS_TOKEN = "cdbcd575-046a-4035-916a-85efc92d63ef";
    private static final String PAYMENT_STATUS_ENDPOINT = String.format("/payment-requests/%s", PAYMENT_ID);

    @Mock
    private StetTokenPaymentPreExecutionResultMapper tokenPaymentPreExecutionResultMapper;

    @Mock
    private StetTokenPaymentHttpRequestInvoker tokenHttpRequestInvoker;

    @Mock
    private AuthenticationMeansSupplier authenticationMeansSupplier;

    @Mock
    private ProviderStateMapper providerStateMapper;

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private DefaultAuthenticationMeans authenticationMeans;

    @Mock
    private TokenResponseDTO tokenResponseDTO;

    @Mock
    private StetTokenPaymentPreExecutionResult tokenPaymentPreExecutionResult;

    private StetStatusPaymentPreExecutionResultMapper statusPaymentPreExecutionResultMapper;

    @BeforeEach
    void initialize() {
        ProviderIdentification providerIdentification = new ProviderIdentification(
                PROVIDER_IDENTIFIER,
                PROVIDER_DISPLAY_NAME,
                ProviderVersion.VERSION_1);

        statusPaymentPreExecutionResultMapper = new StetStatusPaymentPreExecutionResultMapper(
                authenticationMeansSupplier,
                providerIdentification,
                tokenPaymentPreExecutionResultMapper,
                tokenHttpRequestInvoker,
                providerStateMapper);
    }

    @Test
    void shouldMapToStetConfirmationPreExecutionResult() {
        // given
        Map<String, BasicAuthenticationMean> authMeans = new HashMap<>();
        GetStatusRequest request = createGetStatusRequest(authMeans);

        given(authenticationMeansSupplier.getAuthMeans(any(), anyString()))
                .willReturn(authenticationMeans);
        given(tokenPaymentPreExecutionResultMapper.map(any(GetStatusRequest.class), any(DefaultAuthenticationMeans.class)))
                .willReturn(tokenPaymentPreExecutionResult);
        given(tokenHttpRequestInvoker.invokeRequest(any(StetTokenPaymentPreExecutionResult.class)))
                .willReturn(tokenResponseDTO);
        given(tokenResponseDTO.getAccessToken())
                .willReturn(ACCESS_TOKEN);

        // when
        StetConfirmationPreExecutionResult preExecutionResult = statusPaymentPreExecutionResultMapper.map(request);

        // then
        assertThat(preExecutionResult.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(preExecutionResult.getPaymentId()).isEqualTo(PAYMENT_ID);
        assertThat(preExecutionResult.getRequestPath()).isEqualTo(PAYMENT_STATUS_ENDPOINT);
        assertThat(preExecutionResult.getSigner()).isEqualTo(signer);
        assertThat(preExecutionResult.getPsuIpAddress()).isEqualTo(PSU_IP_ADDRESS);
        assertThat(preExecutionResult.getAuthMeans()).isEqualTo(authenticationMeans);
        assertThat(preExecutionResult.getHttpMethod()).isEqualTo(HttpMethod.GET);
        assertThat(preExecutionResult.getRestTemplateManager()).isEqualTo(restTemplateManager);

        then(authenticationMeansSupplier)
                .should()
                .getAuthMeans(authMeans, PROVIDER_IDENTIFIER);
        then(tokenPaymentPreExecutionResultMapper)
                .should()
                .map(request, authenticationMeans);
        then(tokenHttpRequestInvoker)
                .should()
                .invokeRequest(tokenPaymentPreExecutionResult);
        then(tokenResponseDTO)
                .should()
                .getAccessToken();
    }

    private GetStatusRequest createGetStatusRequest(Map<String, BasicAuthenticationMean> authMeans) {
        return new GetStatusRequest(
                PROVIDER_STATE,
                PAYMENT_ID,
                authMeans,
                signer,
                restTemplateManager,
                PSU_IP_ADDRESS,
                new AuthenticationMeansReference(UUID.randomUUID(), UUID.randomUUID()));
    }
}

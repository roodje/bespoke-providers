package com.yolt.providers.knabgroup.common.payment.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.payment.DefaultPisAccessTokenProvider;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentPreExecutionResultMapperTest {

    @InjectMocks
    private DefaultInitiatePaymentPreExecutionResultMapper subject;

    @Mock
    private DefaultPisAccessTokenProvider accessMeansProvider;

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Test
    void shouldReturnDefaultInitiatePaymentPreExecutionResultForMapWhenCorrectData() {
        // given
        Map<String, BasicAuthenticationMean> authMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        SepaInitiatePaymentRequestDTO requestDTO = SepaInitiatePaymentRequestDTO.builder().build();
        InitiatePaymentRequest initiatePaymentRequest = createInitiatePaymentRequest(authMeans, requestDTO);
        KnabGroupAuthenticationMeans knabAuthMeans = KnabGroupAuthenticationMeans.createKnabGroupAuthenticationMeans(authMeans, "provider");
        given(accessMeansProvider.getClientAccessToken(any(KnabGroupAuthenticationMeans.class), any(RestTemplateManager.class)))
                .willReturn("accessToken");
        InitiatePaymentPreExecutionResult expectedResult = new InitiatePaymentPreExecutionResult(
                requestDTO,
                restTemplateManager,
                knabAuthMeans,
                "accessToken",
                signer,
                "baseClientRedirectUrl",
                "state",
                "fakePsuIpAddress"
        );

        // when
        InitiatePaymentPreExecutionResult result = subject.map(initiatePaymentRequest);

        // then
        assertThat(result).usingRecursiveComparison()
                .usingOverriddenEquals()
                .isEqualTo(expectedResult);
    }

    private InitiatePaymentRequest createInitiatePaymentRequest(Map<String, BasicAuthenticationMean> authMeans, SepaInitiatePaymentRequestDTO requestDTO) {
        return new InitiatePaymentRequest(
                requestDTO,
                "baseClientRedirectUrl",
                "state",
                authMeans,
                signer,
                restTemplateManager,
                "fakePsuIpAddress",
                null
        );
    }
}

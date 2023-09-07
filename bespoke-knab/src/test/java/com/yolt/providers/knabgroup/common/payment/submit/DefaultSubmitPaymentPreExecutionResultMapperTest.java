package com.yolt.providers.knabgroup.common.payment.submit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.knabgroup.common.auth.KnabGroupAuthenticationMeans;
import com.yolt.providers.knabgroup.common.payment.DefaultPisAccessTokenProvider;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.PaymentProviderState;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.StatusPaymentPreExecutionResult;
import com.yolt.providers.knabgroup.samples.SampleAuthenticationMeans;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultSubmitPaymentPreExecutionResultMapperTest {

    @Mock
    private DefaultPisAccessTokenProvider accessMeansProvider;

    @Mock
    private Signer signer;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnDefaultStatusPaymentPreExecutionResultForMapWhenCorrectData() throws JsonProcessingException {
        // given
        DefaultSubmitPaymentPreExecutionResultMapper subject = new DefaultSubmitPaymentPreExecutionResultMapper(accessMeansProvider, "provider", objectMapper);
        Map<String, BasicAuthenticationMean> authMeans = SampleAuthenticationMeans.getSampleAuthenticationMeans();
        SubmitPaymentRequest request = createSamplePaymentRequest(authMeans);
        KnabGroupAuthenticationMeans knabAuthMeans = KnabGroupAuthenticationMeans.createKnabGroupAuthenticationMeans(authMeans, "provider");
        given(accessMeansProvider.getClientAccessToken(any(KnabGroupAuthenticationMeans.class), any(RestTemplateManager.class)))
                .willReturn("accessToken");
        given(objectMapper.readValue("state", PaymentProviderState.class)).willReturn(new PaymentProviderState("paymentId", PaymentType.SINGLE));
        StatusPaymentPreExecutionResult expectedResult = new StatusPaymentPreExecutionResult(
                "paymentId",
                restTemplateManager,
                knabAuthMeans,
                "accessToken",
                signer,
                "baseClientRedirectUrl",
                PaymentType.SINGLE
        );

        // when
        StatusPaymentPreExecutionResult result = subject.map(request);

        // then
        assertThat(result).usingRecursiveComparison()
                .usingOverriddenEquals()
                .isEqualTo(expectedResult);
    }

    private SubmitPaymentRequest createSamplePaymentRequest(Map<String, BasicAuthenticationMean> authMeans) {
        return new SubmitPaymentRequest(
                "state",
                authMeans,
                "redirect",
                signer,
                restTemplateManager,
                "baseClientRedirectUrl",
                null
        );
    }
}

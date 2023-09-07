package com.yolt.providers.ing.common.pec.submit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequestBuilder;
import com.yolt.providers.ing.common.IngSampleAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.config.PisBeanConfig;
import com.yolt.providers.ing.common.pec.DefaultPisAccessMeansProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Clock;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultSubmitPaymentPreExecutionResultMapperTest {

    private DefaultSubmitPaymentPreExecutionResultMapper sut;

    @Mock
    private DefaultPisAccessMeansProvider accessMeansProvider;

    @Mock
    private Signer signer;

    @Mock
    private IngClientAccessMeans accessMeans;

    @Mock
    private RestTemplateManager restTemplateManager;

    @Mock
    private Clock clock;

    @Test
    void shouldReturnDefaultInitiatePaymentPreExecutionResultForMapWhenCorrectData() throws IOException, URISyntaxException {
        // given
        ObjectMapper objectMapper = new PisBeanConfig().ingPisObjectMapper();
        sut = new DefaultSubmitPaymentPreExecutionResultMapper(accessMeansProvider, objectMapper, null, clock);

        var authMeans = (new IngSampleAuthenticationMeans()).getAuthenticationMeans();
        var request = createSubmitPaymentRequest(authMeans);

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

    private SubmitPaymentRequest createSubmitPaymentRequest(Map<String, BasicAuthenticationMean> authMeans) {
        return new SubmitPaymentRequestBuilder()
                .setSigner(signer)
                .setAuthenticationMeans(authMeans)
                .setRestTemplateManager(restTemplateManager)
                .setProviderState("{\"paymentId\": \"testPaymentId\"}")
                .setRedirectUrlPostedBackFromSite("http://localhost/callback")
                .setPsuIpAddress("fakePsuIpAddress")
                .build();
    }
}
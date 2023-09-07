package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.ing.common.IngSampleAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngAuthenticationMeans;
import com.yolt.providers.ing.common.auth.IngClientAccessMeans;
import com.yolt.providers.ing.common.pec.DefaultPisAccessMeansProvider;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentPreExecutionResultMapperTest {

    @InjectMocks
    private DefaultInitiatePaymentPreExecutionResultMapper sut;

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
        var authMeans = (new IngSampleAuthenticationMeans()).getAuthenticationMeans();
        var requestDTO = SepaInitiatePaymentRequestDTO.builder().build();
        var initiatePaymentRequest = createInitiatePaymentRequest(authMeans, requestDTO);

        given(accessMeansProvider.getClientAccessMeans(any(IngAuthenticationMeans.class), any(RestTemplateManager.class), any(Signer.class), any(Clock.class)))
                .willReturn(accessMeans);

        // when
        var result = sut.map(initiatePaymentRequest);

        // then
        assertThat(result.getAuthenticationMeans()).isNotNull();
        assertThat(result.getClientAccessMeans()).isNotNull();
        assertThat(result).extracting(DefaultInitiatePaymentPreExecutionResult::getClientAccessMeans,
                DefaultInitiatePaymentPreExecutionResult::getRequestDTO,
                DefaultInitiatePaymentPreExecutionResult::getRestTemplateManager,
                DefaultInitiatePaymentPreExecutionResult::getBaseClientRedirectUrl,
                DefaultInitiatePaymentPreExecutionResult::getPsuIpAddress,
                DefaultInitiatePaymentPreExecutionResult::getSigner,
                DefaultInitiatePaymentPreExecutionResult::getState
        ).contains(accessMeans, requestDTO, restTemplateManager, "baseClientRedirectUrl", "fakePsuIpAddress", signer, "state");
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
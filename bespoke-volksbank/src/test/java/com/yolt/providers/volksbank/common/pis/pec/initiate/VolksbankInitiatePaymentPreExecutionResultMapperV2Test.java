package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.versioning.ProviderVersion;
import com.yolt.providers.volksbank.VolksbankSampleTypedAuthenticationMeans;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import com.yolt.providers.volksbank.common.config.ProviderIdentification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VolksbankInitiatePaymentPreExecutionResultMapperV2Test {

    private VolksbankInitiatePaymentPreExecutionResultMapperV2 subject;

    @Mock
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    void beforeEach() {
        subject = new VolksbankInitiatePaymentPreExecutionResultMapperV2(new ProviderIdentification("VOLKSBANK",
                "Volksbank",
                ProviderVersion.VERSION_1));
    }

    @Test
    void shouldReturnVolksbankSepaInitiatePreExecutionResultForMapWhenCorrectData() {
        // given
        var requestDTO = SepaInitiatePaymentRequestDTO.builder().build();
        var authenticationMeans = new VolksbankSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var initiatePaymentRequest = prepareInitiatePaymentRequest(requestDTO, authenticationMeans);

        // when
        var result = subject.map(initiatePaymentRequest);

        // then
        assertThat(result).extracting(VolksbankSepaInitiatePreExecutionResult::getRequestDTO,
                        VolksbankSepaInitiatePreExecutionResult::getAuthenticationMeans,
                        VolksbankSepaInitiatePreExecutionResult::getRestTemplateManager,
                        VolksbankSepaInitiatePreExecutionResult::getPsuIpAddress,
                        VolksbankSepaInitiatePreExecutionResult::getState,
                        VolksbankSepaInitiatePreExecutionResult::getBaseClientRedirectUrl)
                .contains(requestDTO,
                        VolksbankAuthenticationMeans.fromAuthenticationMeans(authenticationMeans, "VOLKSBANK"),
                        restTemplateManager,
                        "psuIpAddress",
                        "state",
                        "baseClientRedirectUrl");
    }

    private InitiatePaymentRequest prepareInitiatePaymentRequest(SepaInitiatePaymentRequestDTO requestDTO, Map<String, BasicAuthenticationMean> authenticationMeans) {
        return new InitiatePaymentRequest(
                requestDTO,
                "baseClientRedirectUrl",
                "state",
                authenticationMeans,
                null,
                restTemplateManager,
                "psuIpAddress",
                null
        );
    }
}
package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.CbiGlobeSampleTypedAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.model.Token;
import com.yolt.providers.cbiglobe.common.pis.pec.auth.CbiGlobePaymentAccessTokenProvider;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.versioning.ProviderVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CbiGlobeInitiatePaymentPreExecutionResultMapperTest {

    private CbiGlobeInitiatePaymentPreExecutionResultMapper subject;

    @Mock
    private CbiGlobeBaseProperties properties;

    @Mock
    private Signer signer;

    @Mock
    private CbiGlobePaymentAccessTokenProvider paymentAccessTokenProvider;

    @Mock
    private RestTemplateManager restTemplateManager;

    @BeforeEach
    void beforeEach() {
        subject = new CbiGlobeInitiatePaymentPreExecutionResultMapper(
                paymentAccessTokenProvider,
                new ProviderIdentification("CBI_GLOBE", "Cbi Globe", ProviderVersion.VERSION_1),
                properties);
    }

    @Test
    void shouldReturnCbiGlobeSepaInitiatePreExecutionResultForMapWhenCorrectData() {
        // given
        given(properties.getFirstAspspData())
                .willReturn(new AspspData());

        var requestDTO = SepaInitiatePaymentRequestDTO.builder().build();
        var authenticationMeans = new CbiGlobeSampleTypedAuthenticationMeans().getAuthenticationMeans();
        var cbiGlobeAuthMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(authenticationMeans, "CBI_GLOBE");
        var initiatePaymentRequest = prepareInitiatePaymentRequest(requestDTO, authenticationMeans);
        var aspspData = properties.getFirstAspspData();
        var token = new Token();
        token.setAccessToken("accessToken");

        given(paymentAccessTokenProvider.provideClientAccessToken(restTemplateManager, cbiGlobeAuthMeans, null)).willReturn(token);

        // when
        var result = subject.map(initiatePaymentRequest);

        // then
        assertThat(result).extracting(CbiGlobeSepaInitiatePreExecutionResult::getRequestDTO,
                CbiGlobeSepaInitiatePreExecutionResult::getAuthenticationMeans,
                CbiGlobeSepaInitiatePreExecutionResult::getRestTemplateManager,
                CbiGlobeSepaInitiatePreExecutionResult::getPsuIpAddress,
                CbiGlobeSepaInitiatePreExecutionResult::getAccessToken,
                CbiGlobeSepaInitiatePreExecutionResult::getAspspData,
                CbiGlobeSepaInitiatePreExecutionResult::getRedirectUrlWithState)
                .contains(requestDTO,
                        cbiGlobeAuthMeans,
                        restTemplateManager,
                        "psuIpAddress",
                        "accessToken",
                        aspspData,
                        "baseClientRedirectUrl?state=state");
        assertThat(result.getSignatureData()).isNotNull();
    }

    private InitiatePaymentRequest prepareInitiatePaymentRequest(SepaInitiatePaymentRequestDTO requestDTO, Map<String, BasicAuthenticationMean> authenticationMeans) {
        return new InitiatePaymentRequest(
                requestDTO,
                "baseClientRedirectUrl",
                "state",
                authenticationMeans,
                signer,
                restTemplateManager,
                "psuIpAddress",
                null
        );
    }
}
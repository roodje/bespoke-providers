package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.pis.pec.auth.CbiGlobePaymentAccessTokenProvider;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaInitiatePaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
public class CbiGlobeInitiatePaymentPreExecutionResultMapper implements SepaInitiatePaymentPreExecutionResultMapper<CbiGlobeSepaInitiatePreExecutionResult> {

    private final CbiGlobePaymentAccessTokenProvider paymentAccessTokenProvider;
    private final ProviderIdentification providerIdentification;
    private final CbiGlobeBaseProperties properties;

    @Override
    public CbiGlobeSepaInitiatePreExecutionResult map(InitiatePaymentRequest initiatePaymentRequest) {
        var restTemplateManager = initiatePaymentRequest.getRestTemplateManager();
        var authMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(initiatePaymentRequest.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        var redirectUrlWithState = UriComponentsBuilder.fromUriString(initiatePaymentRequest.getBaseClientRedirectUrl())
                .queryParam("state", initiatePaymentRequest.getState()).toUriString();

        var accessMeans = paymentAccessTokenProvider.provideClientAccessToken(restTemplateManager, authMeans, initiatePaymentRequest.getAuthenticationMeansReference());

        return CbiGlobeSepaInitiatePreExecutionResult.builder()
                .requestDTO(initiatePaymentRequest.getRequestDTO())
                .authenticationMeans(authMeans)
                .restTemplateManager(restTemplateManager)
                .psuIpAddress(initiatePaymentRequest.getPsuIpAddress())
                .accessToken(accessMeans.getAccessToken())
                .aspspData(properties.getFirstAspspData())
                .signatureData(authMeans.getSigningData(initiatePaymentRequest.getSigner()))
                .redirectUrlWithState(redirectUrlWithState)
                .build();
    }
}

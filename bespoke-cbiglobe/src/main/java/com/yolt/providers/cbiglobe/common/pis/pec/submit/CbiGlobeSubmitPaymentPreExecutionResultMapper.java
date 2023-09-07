package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePaymentProviderState;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePaymentProviderStateDeserializer;
import com.yolt.providers.cbiglobe.common.pis.pec.auth.CbiGlobePaymentAccessTokenProvider;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.submit.SepaSubmitPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CbiGlobeSubmitPaymentPreExecutionResultMapper implements SepaSubmitPaymentPreExecutionResultMapper<CbiGlobeSepaSubmitPreExecutionResult> {

    private final CbiGlobePaymentAccessTokenProvider paymentAccessTokenProvider;
    private final CbiGlobePaymentProviderStateDeserializer providerStateDeserializer;
    private final ProviderIdentification providerIdentification;
    private final CbiGlobeBaseProperties properties;

    @Override
    public CbiGlobeSepaSubmitPreExecutionResult map(SubmitPaymentRequest submitPaymentRequest) {
        var redirectUrlPostedBackFromSite = submitPaymentRequest.getRedirectUrlPostedBackFromSite();
        verifyRedirectSuccessful(redirectUrlPostedBackFromSite);
        var authenticationMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(submitPaymentRequest.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        var restTemplateManager = submitPaymentRequest.getRestTemplateManager();

        var accessMeans = paymentAccessTokenProvider.provideClientAccessToken(restTemplateManager, authenticationMeans, submitPaymentRequest.getAuthenticationMeansReference());

        CbiGlobePaymentProviderState providerState = providerStateDeserializer.deserialize(submitPaymentRequest.getProviderState());

        return CbiGlobeSepaSubmitPreExecutionResult.builder()
                .authenticationMeans(authenticationMeans)
                .restTemplateManager(restTemplateManager)
                .paymentId(providerState.getPaymentId())
                .accessToken(accessMeans.getAccessToken())
                .aspspData(properties.getFirstAspspData())
                .signatureData(authenticationMeans.getSigningData(submitPaymentRequest.getSigner()))
                .build();
    }

    private void verifyRedirectSuccessful(String redirectUrlPostedBackFromSite) {
        if (redirectUrlPostedBackFromSite.contains("error")) {
            throw new IllegalStateException("Got error in callback URL. Payment confirmation failed. Redirect url: " + redirectUrlPostedBackFromSite);
        }
    }
}

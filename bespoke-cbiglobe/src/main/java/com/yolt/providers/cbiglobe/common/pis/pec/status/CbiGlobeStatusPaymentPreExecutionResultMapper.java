package com.yolt.providers.cbiglobe.common.pis.pec.status;

import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import com.yolt.providers.cbiglobe.common.config.ProviderIdentification;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePaymentProviderState;
import com.yolt.providers.cbiglobe.common.pis.pec.CbiGlobePaymentProviderStateDeserializer;
import com.yolt.providers.cbiglobe.common.pis.pec.auth.CbiGlobePaymentAccessTokenProvider;
import com.yolt.providers.cbiglobe.common.pis.pec.submit.CbiGlobeSepaSubmitPreExecutionResult;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.status.SepaStatusPaymentPreExecutionResultMapper;
import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class CbiGlobeStatusPaymentPreExecutionResultMapper implements SepaStatusPaymentPreExecutionResultMapper<CbiGlobeSepaSubmitPreExecutionResult> {

    private final CbiGlobePaymentAccessTokenProvider paymentAccessTokenProvider;
    private final CbiGlobePaymentProviderStateDeserializer providerStateDeserializer;
    private final ProviderIdentification providerIdentification;
    private final CbiGlobeBaseProperties properties;

    @Override
    public CbiGlobeSepaSubmitPreExecutionResult map(GetStatusRequest getStatusRequest) {
        String paymentId = extractPaymentId(getStatusRequest);
        var authenticationMeans = CbiGlobeAuthenticationMeans.getCbiGlobeAuthenticationMeans(getStatusRequest.getAuthenticationMeans(), providerIdentification.getProviderIdentifier());
        var restTemplateManager = getStatusRequest.getRestTemplateManager();

        var accessMeans = paymentAccessTokenProvider.provideClientAccessToken(restTemplateManager, authenticationMeans, getStatusRequest.getAuthenticationMeansReference());

        return CbiGlobeSepaSubmitPreExecutionResult.builder()
                .authenticationMeans(authenticationMeans)
                .restTemplateManager(restTemplateManager)
                .paymentId(paymentId)
                .accessToken(accessMeans.getAccessToken())
                .aspspData(properties.getFirstAspspData())
                .signatureData(authenticationMeans.getSigningData(getStatusRequest.getSigner()))
                .build();
    }

    private String extractPaymentId(GetStatusRequest getStatusRequest) {
        if (!StringUtils.isEmpty(getStatusRequest.getPaymentId())) {
            return getStatusRequest.getPaymentId();
        }

        CbiGlobePaymentProviderState providerState = providerStateDeserializer.deserialize(getStatusRequest.getProviderState());
        return providerState.getPaymentId();
    }
}

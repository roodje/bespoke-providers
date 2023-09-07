package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.Region;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Deprecated
public class StetTokenPaymentPreExecutionResultMapper implements SepaTokenPaymentPreExecutionResultMapper<StetTokenPaymentPreExecutionResult> {

    private final DefaultProperties properties;

    @Override
    public StetTokenPaymentPreExecutionResult map(InitiatePaymentRequest request, DefaultAuthenticationMeans authMeans) {
        Region region = getRegion();
        return StetTokenPaymentPreExecutionResult.builder()
                .requestUrl(region.getTokenUrl())
                .baseUrl(region.getBaseUrl())
                .authMeans(authMeans)
                .restTemplateManager(request.getRestTemplateManager())
                .signer(request.getSigner())
                .build();
    }

    @Override
    public StetTokenPaymentPreExecutionResult map(GetStatusRequest request, DefaultAuthenticationMeans authMeans) {
        Region region = getRegion();
        return StetTokenPaymentPreExecutionResult.builder()
                .requestUrl(region.getTokenUrl())
                .baseUrl(region.getBaseUrl())
                .authMeans(authMeans)
                .restTemplateManager(request.getRestTemplateManager())
                .signer(request.getSigner())
                .build();
    }

    @Override
    public StetTokenPaymentPreExecutionResult map(SubmitPaymentRequest request, DefaultAuthenticationMeans authMeans) {
        Region region = getRegion();
        return StetTokenPaymentPreExecutionResult.builder()
                .requestUrl(region.getTokenUrl())
                .baseUrl(region.getBaseUrl())
                .authMeans(authMeans)
                .restTemplateManager(request.getRestTemplateManager())
                .signer(request.getSigner())
                .build();
    }

    private Region getRegion() {
        return properties.getRegions().get(0);
    }
}

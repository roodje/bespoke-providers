package com.yolt.providers.stet.boursoramagroup.common.service.pec.authorization.token;

import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.domain.Region;
import com.yolt.providers.stet.generic.service.pec.authorization.token.SepaTokenPaymentPreExecutionResultMapper;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BoursoramaGroupTokenPaymentPreExecutionResultMapper implements SepaTokenPaymentPreExecutionResultMapper<StetTokenPaymentPreExecutionResult> {

    private final DefaultProperties properties;

    @Override
    public StetTokenPaymentPreExecutionResult map(InitiatePaymentRequest request, DefaultAuthenticationMeans authMeans) {
        Region region = getRegion();
        return StetTokenPaymentPreExecutionResult.builder()
                .requestUrl(getTokenUrl(region))
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
                .requestUrl(getTokenUrl(region))
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
                .requestUrl(getTokenUrl(region))
                .baseUrl(region.getBaseUrl())
                .authMeans(authMeans)
                .restTemplateManager(request.getRestTemplateManager())
                .signer(request.getSigner())
                .build();
    }

    private String getTokenUrl(Region region) {
        return region.getTokenUrl() + "token";
    }

    private Region getRegion() {
        return properties.getRegions().get(0);
    }
}

package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.pis.sepa.GetStatusRequest;
import com.yolt.providers.common.pis.sepa.InitiatePaymentRequest;
import com.yolt.providers.common.pis.sepa.SubmitPaymentRequest;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Region;


public class StetTokenPaymentPreExecutionResultMapperV2 implements SepaTokenPaymentPreExecutionResultMapperV2<StetTokenPaymentPreExecutionResult> {

    @Override
    public StetTokenPaymentPreExecutionResult map(InitiatePaymentRequest request, DefaultAuthenticationMeans authMeans, HttpClient httpClient, Region region) {
        return StetTokenPaymentPreExecutionResult.builder()
                .requestUrl(region.getTokenUrl())
                .authMeans(authMeans)
                .httpClient(httpClient)
                .signer(request.getSigner())
                .build();
    }

    @Override
    public StetTokenPaymentPreExecutionResult map(GetStatusRequest request, DefaultAuthenticationMeans authMeans, HttpClient httpClient, Region region) {
        return StetTokenPaymentPreExecutionResult.builder()
                .requestUrl(region.getTokenUrl())
                .authMeans(authMeans)
                .httpClient(httpClient)
                .signer(request.getSigner())
                .build();
    }

    @Override
    public StetTokenPaymentPreExecutionResult map(SubmitPaymentRequest request, DefaultAuthenticationMeans authMeans, HttpClient httpClient, Region region) {
        return StetTokenPaymentPreExecutionResult.builder()
                .requestUrl(region.getTokenUrl())
                .authMeans(authMeans)
                .httpClient(httpClient)
                .signer(request.getSigner())
                .build();
    }
}

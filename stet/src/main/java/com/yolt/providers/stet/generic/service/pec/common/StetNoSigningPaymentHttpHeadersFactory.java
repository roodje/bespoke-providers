package com.yolt.providers.stet.generic.service.pec.common;

import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class StetNoSigningPaymentHttpHeadersFactory implements StetPaymentHttpHeadersFactory {

    private final Supplier<String> lastExternalTraceIdSupplier;

    public StetNoSigningPaymentHttpHeadersFactory() {
        this.lastExternalTraceIdSupplier = ExternalTracingUtil::createLastExternalTraceId;
    }

    @Override
    public HttpHeaders createPaymentAccessTokenHttpHeaders(StetTokenPaymentPreExecutionResult preExecutionResult,
                                                           MultiValueMap<String, String> requestBody) {
        DefaultAuthenticationMeans authMeans = preExecutionResult.getAuthMeans();
        return HttpHeadersBuilder.builder()
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
                .withBasicAuthorization(authMeans.getClientId(), authMeans.getClientSecret())
                .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .build();
    }

    @Override
    public HttpHeaders createPaymentInitiationHttpHeaders(StetInitiatePreExecutionResult preExecutionResult,
                                                          StetPaymentInitiationRequestDTO requestDTO) {
        return prepareCommonHttpHeaders(preExecutionResult.getAccessToken(), preExecutionResult.getPsuIpAddress());
    }

    @Override
    public HttpHeaders createPaymentSubmitHttpHeaders(StetConfirmationPreExecutionResult preExecutionResult,
                                                      StetPaymentConfirmationRequestDTO requestDTO) {
        return prepareCommonHttpHeaders(preExecutionResult.getAccessToken(), preExecutionResult.getPsuIpAddress());
    }

    @Override
    public HttpHeaders createPaymentStatusHttpHeaders(StetConfirmationPreExecutionResult preExecutionResult) {
        return prepareCommonHttpHeaders(preExecutionResult.getAccessToken(), preExecutionResult.getPsuIpAddress());
    }

    protected HttpHeaders prepareCommonHttpHeaders(String accessToken, String psuIpAddress) {
        return HttpHeadersBuilder.builder()
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(accessToken)
                .withContentType(APPLICATION_JSON)
                .withPsuIpAddress(psuIpAddress)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .build();
    }
}

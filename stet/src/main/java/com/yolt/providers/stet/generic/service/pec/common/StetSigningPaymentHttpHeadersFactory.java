package com.yolt.providers.stet.generic.service.pec.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.initiate.StetInitiatePreExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class StetSigningPaymentHttpHeadersFactory implements StetPaymentHttpHeadersFactory {

    protected final HttpSigner httpSigner;
    private final Supplier<String> lastExternalTraceIdSupplier;

    public StetSigningPaymentHttpHeadersFactory(HttpSigner httpSigner) {
        this.httpSigner = httpSigner;
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
        SignatureData signatureData = prepareSignatureData(
                preExecutionResult.getSigner(),
                preExecutionResult.getAuthMeans(),
                preExecutionResult.getHttpMethod(),
                preExecutionResult.getRequestPath());

        return prepareCommonHttpHeaders(
                signatureData,
                preExecutionResult.getAccessToken(),
                preExecutionResult.getPsuIpAddress(),
                requestDTO);
    }

    @Override
    public HttpHeaders createPaymentSubmitHttpHeaders(StetConfirmationPreExecutionResult preExecutionResult,
                                                      StetPaymentConfirmationRequestDTO requestDTO) {
        SignatureData signatureData = prepareSignatureData(
                preExecutionResult.getSigner(),
                preExecutionResult.getAuthMeans(),
                preExecutionResult.getHttpMethod(),
                preExecutionResult.getRequestPath());

        return prepareCommonHttpHeaders(
                signatureData,
                preExecutionResult.getAccessToken(),
                preExecutionResult.getPsuIpAddress(),
                requestDTO);
    }

    @Override
    public HttpHeaders createPaymentStatusHttpHeaders(StetConfirmationPreExecutionResult preExecutionResult) {
        SignatureData signatureData = prepareSignatureData(
                preExecutionResult.getSigner(),
                preExecutionResult.getAuthMeans(),
                preExecutionResult.getHttpMethod(),
                preExecutionResult.getRequestPath());

        return prepareCommonHttpHeaders(
                signatureData,
                preExecutionResult.getAccessToken(),
                preExecutionResult.getPsuIpAddress(),
                new byte[0]);
    }

    protected SignatureData prepareSignatureData(Signer signer, DefaultAuthenticationMeans authMeans, HttpMethod httpMethod, String url) {
        return new SignatureData(
                signer,
                authMeans.getSigningKeyIdHeader(),
                authMeans.getClientSigningKeyId(),
                authMeans.getClientSigningCertificate(),
                httpMethod,
                url);
    }

    protected HttpHeaders prepareCommonHttpHeaders(SignatureData signatureData,
                                                   String accessToken,
                                                   String psuIpAddress,
                                                   Object requestBody) {
        return HttpHeadersBuilder.builder(httpSigner)
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(accessToken)
                .withContentType(APPLICATION_JSON)
                .withPsuIpAddress(psuIpAddress)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .signAndBuild(signatureData, requestBody);
    }
}

package com.yolt.providers.stet.societegeneralegroup.common.service.pec.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.pec.common.StetSigningPaymentHttpHeadersFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.function.Supplier;

import static org.springframework.http.MediaType.APPLICATION_JSON;

public class SocieteGeneraleGroupSigningPaymentHttpHeadersFactory extends StetSigningPaymentHttpHeadersFactory {

    private final Supplier<String> lastExternalTraceIdSupplier;

    public SocieteGeneraleGroupSigningPaymentHttpHeadersFactory(HttpSigner httpSigner, Supplier<String> lastExternalTraceIdSupplier) {
        super(httpSigner, lastExternalTraceIdSupplier);
        this.lastExternalTraceIdSupplier = lastExternalTraceIdSupplier;
    }


    @Override
    protected HttpHeaders prepareCommonHttpHeaders(SignatureData signatureData, String accessToken, String psuIpAddress, Object requestBody) {
        return HttpHeadersBuilder.builder(httpSigner)
                .withAccept(Collections.singletonList(APPLICATION_JSON))
                .withBearerAuthorization(accessToken)
                .withContentType(APPLICATION_JSON)
                .withPsuIpAddress(psuIpAddress)
                .withCustomXRequestId(lastExternalTraceIdSupplier)
                .withCustomHeader("Client-id", signatureData.getClientId())
                .signAndBuild(signatureData, requestBody);
    }

    @Override
    protected SignatureData prepareSignatureData(Signer signer, DefaultAuthenticationMeans authMeans, HttpMethod httpMethod, String url) {
        return new SignatureData(
                signer,
                authMeans.getSigningKeyIdHeader(),
                authMeans.getClientSigningKeyId(),
                authMeans.getClientSigningCertificate(),
                httpMethod,
                null,
                url,
                authMeans.getClientId());
    }
}

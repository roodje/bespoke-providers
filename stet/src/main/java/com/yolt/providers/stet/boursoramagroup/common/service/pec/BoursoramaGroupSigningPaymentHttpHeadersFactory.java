package com.yolt.providers.stet.boursoramagroup.common.service.pec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.pec.authorization.token.StetTokenPaymentPreExecutionResult;
import com.yolt.providers.stet.generic.service.pec.common.StetSigningPaymentHttpHeadersFactory;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

public class BoursoramaGroupSigningPaymentHttpHeadersFactory extends StetSigningPaymentHttpHeadersFactory {

    private static final String URL_PISP_AUTHORIZATION = "/services/api/v1.7/_public_/authentication/oauth/token";

    private final ObjectMapper objectMapper;

    public BoursoramaGroupSigningPaymentHttpHeadersFactory(HttpSigner httpSigner, ObjectMapper objectMapper) {
        super(httpSigner);
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpHeaders createPaymentAccessTokenHttpHeaders(StetTokenPaymentPreExecutionResult preExecutionResult,
                                                           MultiValueMap<String, String> requestBody) {
        byte[] serializedRequestBody = getSerializedRequestBody(requestBody.toSingleValueMap());

        DefaultAuthenticationMeans authMeans = preExecutionResult.getAuthMeans();
        SignatureData signatureData = new SignatureData(
                preExecutionResult.getSigner(),
                authMeans.getSigningKeyIdHeader(),
                authMeans.getClientSigningKeyId(),
                authMeans.getClientSigningCertificate(),
                HttpMethod.POST,
                URI.create(preExecutionResult.getRequestUrl()).getHost(),
                URL_PISP_AUTHORIZATION);

        return HttpHeadersBuilder.builder(httpSigner)
                .withContentType(MediaType.APPLICATION_JSON)
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
                .withCustomXRequestId(ExternalTracingUtil.createLastExternalTraceId())
                .signAndBuild(signatureData, serializedRequestBody);
    }

    @SneakyThrows
    private byte[] getSerializedRequestBody(Object body) {
        return objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
    }
}

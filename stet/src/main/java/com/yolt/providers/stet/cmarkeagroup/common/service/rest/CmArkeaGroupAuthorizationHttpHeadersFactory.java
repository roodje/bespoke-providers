package com.yolt.providers.stet.cmarkeagroup.common.service.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.authorization.request.TokenRequest;
import com.yolt.providers.stet.generic.service.authorization.rest.header.AuthorizationHttpHeadersFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@RequiredArgsConstructor
public class CmArkeaGroupAuthorizationHttpHeadersFactory implements AuthorizationHttpHeadersFactory {

    private final ObjectMapper objectMapper;
    private final HttpSigner signer;

    @Override
    public HttpHeaders createAccessTokenHeaders(DefaultAuthenticationMeans authMeans, Object body, TokenRequest request) {
        byte[] serializedRequestBody = getSerializedRequestBody(body);

        SignatureData signatureData = new SignatureData(
                request.getSigner(),
                authMeans.getSigningKeyIdHeader(),
                authMeans.getClientSigningKeyId(),
                authMeans.getClientSigningCertificate(),
                HttpMethod.POST,
                URI.create(request.getTokenUrl()).getHost(),
                request.getTokenUrl()
        );

        HttpHeaders headers = HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_FORM_URLENCODED)
                .withAccept(Collections.singletonList(MediaType.valueOf("application/hal+json;charset=utf-8")))
                .withCustomXRequestId(ExternalTracingUtil.createLastExternalTraceId())
                .build();

        return HttpHeadersBuilder.enhancing(headers, signer)
                .signAndBuild(signatureData, serializedRequestBody);

    }

    @SneakyThrows
    public byte[] getSerializedRequestBody(Object body) {
        return objectMapper.writeValueAsString(body).getBytes(StandardCharsets.UTF_8);
    }
}

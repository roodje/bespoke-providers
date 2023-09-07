package com.yolt.providers.stet.boursoramagroup.common.service.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.http.signer.HttpSigner;
import com.yolt.providers.stet.generic.service.authorization.request.AccessTokenRequest;
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
public class BoursoramaGroupAuthorizationHttpHeadersFactory implements AuthorizationHttpHeadersFactory {

    private final ObjectMapper objectMapper;
    private final HttpSigner signer;

    private static final String ACCESS_TOKEN_PATH = "/services/api/v1.7/_public_/authentication/oauth/consumeauthorizationcode";

    @Override
    public HttpHeaders createAccessTokenHeaders(DefaultAuthenticationMeans authMeans, Object body, TokenRequest request) {
        byte[] serializedRequestBody = getSerializedRequestBody(body);

        String endpoint = request instanceof AccessTokenRequest ? ACCESS_TOKEN_PATH : request.getTokenUrl();

        SignatureData signatureData = new SignatureData(
                request.getSigner(),
                authMeans.getSigningKeyIdHeader(),
                authMeans.getClientSigningKeyId(),
                authMeans.getClientSigningCertificate(),
                HttpMethod.POST,
                URI.create(request.getTokenUrl()).getHost(),
                endpoint);

        HttpHeaders headers = HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_JSON)
                .withAccept(Collections.singletonList(MediaType.APPLICATION_JSON))
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
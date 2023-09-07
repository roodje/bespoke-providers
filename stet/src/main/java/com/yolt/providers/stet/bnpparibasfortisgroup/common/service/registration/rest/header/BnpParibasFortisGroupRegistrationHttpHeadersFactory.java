package com.yolt.providers.stet.bnpparibasfortisgroup.common.service.registration.rest.header;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.stet.generic.http.headers.HttpHeadersBuilder;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.service.registration.rest.header.RegistrationHttpHeadersFactory;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwx.Headers;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
public class BnpParibasFortisGroupRegistrationHttpHeadersFactory implements RegistrationHttpHeadersFactory {

    private static final String X_JWS_SIGNATURE_HEADER = "x-jws-signature";

    private final ObjectMapper objectMapper;

    @Override
    public HttpHeaders createRegistrationHttpHeaders(RegistrationRequest registerRequest, Object body, HttpMethod httpMethod, String url) {
        String requestId = UUID.randomUUID().toString();
        return HttpHeadersBuilder.builder()
                .withContentType(MediaType.APPLICATION_JSON)
                .withCustomXRequestId(requestId)
                .withCustomHeader(X_JWS_SIGNATURE_HEADER, createJwsSignature(registerRequest, body, requestId, httpMethod, url))
                .build();
    }

    private String createJwsSignature(RegistrationRequest registrationRequest, Object body, String requestId, HttpMethod method, String url) {
        JsonWebSignature jws = new JsonWebSignature();
        Headers jwsHeaders = jws.getHeaders();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jwsHeaders.setObjectHeaderValue("typ", "JOSE");
        jws.setCriticalHeaderNames("x5u", "url", "aud", "txn", "iat", "exp");

        URI registrationUrl = URI.create(url);
        jwsHeaders.setObjectHeaderValue("x5u", registrationRequest.getAuthMeans().getSigningKeyIdHeader());
        jwsHeaders.setObjectHeaderValue("url", String.format("%s %s", method.toString().toLowerCase(), registrationUrl.getPath()));
        jwsHeaders.setObjectHeaderValue("aud", registrationUrl.getHost());
        jwsHeaders.setObjectHeaderValue("txn", requestId);
        jwsHeaders.setObjectHeaderValue("iat", getCurrentNumericDate());
        jwsHeaders.setObjectHeaderValue("exp", getExpirationNumericDate());

        try {
            jws.setPayload(objectMapper.writeValueAsString(body));
            return registrationRequest.getSigner()
                    .sign(jws, registrationRequest.getAuthMeans().getClientSigningKeyId(), SignatureAlgorithm.SHA256_WITH_RSA)
                    .getDetachedContentCompactSerialization();
        } catch (JsonProcessingException e) {
            throw new AutoOnboardingException(registrationRequest.getProviderIdentifier(), "Failed to serialize registration body for JWS signature", e);
        }
    }

    private static Long getCurrentNumericDate() {
        return NumericDate.now().getValue();
    }

    private static Long getExpirationNumericDate() {
        NumericDate numericDate = NumericDate.now();
        numericDate.addSeconds(30);
        return numericDate.getValue();
    }
}

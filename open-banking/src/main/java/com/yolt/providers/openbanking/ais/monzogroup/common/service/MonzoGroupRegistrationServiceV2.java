package com.yolt.providers.openbanking.ais.monzogroup.common.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.monzogroup.common.MonzoGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.restclient.MonzoGroupRegistrationRestClientV2;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.CODE;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.ID_TOKEN;
import static com.yolt.providers.openbanking.ais.monzogroup.common.auth.MonzoGroupAuthMeansMapper.*;

@RequiredArgsConstructor
public class MonzoGroupRegistrationServiceV2 {

    private final MonzoGroupRegistrationRestClientV2 restClient;
    private final MonzoGroupPropertiesV2 properties;

    private static final String REDIRECT_URIS_CLAIM = "redirect_uris";
    private static final String TOKEN_ENDPOINT_AUTH_METHODS_CLAIM = "token_endpoint_auth_method";
    private static final String AUTH_METHOD = "tls_client_auth";
    private static final String GRANT_TYPES_CLAIM = "grant_types";
    private static final String SOFTWARE_STATEMENT_CLAIM = "software_statement";
    private static final String SOFTWARE_ID_CLAIM = "software_id";
    private static final String SCOPE_CLAIM = "scope";
    private static final String RESPONSE_TYPES_CLAIM = "response_types";
    private static final String APPLICATION_TYPE_CLAIM = "application_type";
    private static final String APPLICATION_TYPE = "web";
    private static final String ID_TOKEN_SIGNING_ALG_CLAIM = "id_token_signing_alg";
    private static final String REQUEST_OBJECT_SIGNING_CLAIM = "request_object_signing_alg";
    private static final String ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM = "id_token_signed_response_alg";
    private static final String TLS_CLIENT_AUTH_SUBJECT_DN_CLAIM = "tls_client_auth_subject_dn";
    private static final String TYP_HEADER = "typ";
    private static final String JWT = "JWT";
    private static final String SIGNATURE_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

    @NonNull
    public Optional<AutoOnboardingResponse> register(HttpClient httpClient,
                                                     UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                     String scope) throws TokenInvalidException, CertificateException {
        if (!isRegistrationRequestValid(urlAutoOnboardingRequest)) {
            return Optional.empty();
        }
        return Optional.ofNullable(invokeRegistration(httpClient,
                urlAutoOnboardingRequest.getAuthenticationMeans(),
                urlAutoOnboardingRequest,
                scope));
    }

    @NonNull
    private AutoOnboardingResponse invokeRegistration(HttpClient httpClient,
                                                      Map<String, BasicAuthenticationMean> authenticationMeans,
                                                      UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                      String scope) throws TokenInvalidException, CertificateException {
        String signingKeyId = authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue();

        JsonWebSignature jws = createJws(authenticationMeans, urlAutoOnboardingRequest, scope);
        String payload = urlAutoOnboardingRequest.getSigner().sign(jws, UUID.fromString(signingKeyId), SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(SIGNATURE_ALGORITHM)).getCompactSerialization();

        return restClient.register(httpClient, payload, properties.getRegistrationUrl());
    }

    private JsonWebSignature createJws(Map<String, BasicAuthenticationMean> authenticationMeans,
                                       UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                       String scope) throws CertificateException {
        JwtClaims claims = new JwtClaims();
        claims.setAudience(properties.getRegistrationAudience());
        claims.setIssuer(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());

        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHODS_CLAIM, AUTH_METHOD);
        claims.setClaim(GRANT_TYPES_CLAIM, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim(RESPONSE_TYPES_CLAIM, Collections.singletonList(CODE + " " + ID_TOKEN));
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, authenticationMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(SOFTWARE_ID_CLAIM, authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        claims.setClaim(SCOPE_CLAIM, scope);
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNING_ALG_CLAIM, SIGNATURE_ALGORITHM);
        claims.setClaim(REQUEST_OBJECT_SIGNING_CLAIM, SIGNATURE_ALGORITHM);
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, SIGNATURE_ALGORITHM);
        claims.setClaim(TLS_CLIENT_AUTH_SUBJECT_DN_CLAIM, getSubjectDomainName(authenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(SIGNATURE_ALGORITHM);
        jws.setKeyIdHeaderValue(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }

    private Object getSubjectDomainName(String certificatePem) throws CertificateException {
        X509Certificate x509Certificate = KeyUtil.createCertificateFromPemFormat(certificatePem);
        return x509Certificate.getSubjectDN().getName();
    }

    private boolean isRegistrationRequestValid(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        return RegistrationOperation.UPDATE.equals(urlAutoOnboardingRequest.getRegistrationOperation()) ||
                (RegistrationOperation.CREATE.equals(urlAutoOnboardingRequest.getRegistrationOperation()) &&
                        !urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(CLIENT_ID_NAME));
    }

}
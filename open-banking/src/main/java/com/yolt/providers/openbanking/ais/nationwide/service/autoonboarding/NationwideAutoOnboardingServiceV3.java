package com.yolt.providers.openbanking.ais.nationwide.service.autoonboarding;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.nationwide.NationwidePropertiesV2;
import com.yolt.providers.openbanking.ais.nationwide.service.restclient.NationwideRestClientAisV7;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;
import static com.yolt.providers.openbanking.ais.nationwide.auth.NationwideAuthMeansBuilderV3.*;

@RequiredArgsConstructor
public class NationwideAutoOnboardingServiceV3 {

    private static final String REDIRECT_URIS_CLAIM = "redirect_uris";
    private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLAIM = "token_endpoint_auth_method";
    private static final String TLS_CLIENT_AUTH_DN_CLAIM = "tls_client_auth_subject_dn";
    private static final String GRANT_TYPES_CLAIM = "grant_types";
    private static final String RESPONSE_TYPES_CLAIM = "response_types";
    private static final String SOFTWARE_ID_CLAIM = "software_id";
    private static final String SCOPE_CLAIM = "scope";
    private static final String SOFTWARE_STATEMENT_CLAIM = "software_statement";
    private static final String APPLICATION_TYPE_CLAIM = "application_type";
    private static final String ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM = "id_token_signed_response_alg";
    private static final String REQUEST_OBJECT_SIGNING_ALG_CLAIM = "request_object_signing_alg";

    private static final float EXPIRATION_TIME_MINUTES_IN_THE_FUTURE = 60;
    private static final List<String> RESPONSE_TYPES = Collections.singletonList("code id_token");
    private static final String APPLICATION_TYPE = "web";
    private static final String TYP_HEADER = "typ";
    private static final String JWT = "JWT";
    private static final String AUTH_METHOD = "tls_client_auth";
    private static final String SIGNATURE_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final String CLIENT_ID_URL = "/{client-id}";

    private final NationwideRestClientAisV7 restClient;
    private final NationwidePropertiesV2 properties;
    private final AuthenticationService authenticationService;

    public Optional<AutoOnboardingResponse> register(HttpClient httpClient,
                                                     DefaultAuthMeans defaultAuthMeans,
                                                     TokenScope tokenScope,
                                                     UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException, CertificateException {
        return Optional.ofNullable(invokeRegistration(httpClient, defaultAuthMeans, tokenScope, urlAutoOnboardingRequest));
    }

    protected AutoOnboardingResponse invokeRegistration(HttpClient httpClient,
                                                        DefaultAuthMeans defaultAuthMeans,
                                                        TokenScope tokenScope,
                                                        UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException, CertificateException {
        Map<String, BasicAuthenticationMean> authMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        JsonWebSignature jws = createJws(authMeans, urlAutoOnboardingRequest);

        Signer signer = urlAutoOnboardingRequest.getSigner();
        String payload = signer.sign(jws, UUID.fromString(authMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()),
                SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(SIGNATURE_ALGORITHM))
                .getCompactSerialization();
        if (StringUtils.isEmpty(defaultAuthMeans.getClientId())) {
            return restClient.register(httpClient, payload, properties.getRegistrationUrl());
        }
        AccessMeans accessMeans = authenticationService.getClientAccessTokenWithoutCache(httpClient, defaultAuthMeans, tokenScope, signer);
        String requestUrl = properties.getRegistrationUrl() + CLIENT_ID_URL;
        return restClient.updateRegistration(httpClient, payload, requestUrl, accessMeans, defaultAuthMeans.getClientId());
    }

    protected JsonWebSignature createJws(Map<String, BasicAuthenticationMean> authMeans,
                                         UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws CertificateException {
        String softwareId = authMeans.get(SOFTWARE_ID_NAME).getValue();

        JwtClaims claims = new JwtClaims();
        claims.setAudience(authMeans.get(INSTITUTION_ID_NAME).getValue());
        claims.setIssuer(softwareId);
        claims.setIssuedAtToNow();
        claims.setJwtId(UUID.randomUUID().toString().toLowerCase());
        claims.setExpirationTimeMinutesInTheFuture(EXPIRATION_TIME_MINUTES_IN_THE_FUTURE);

        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, authMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM, AUTH_METHOD);
        claims.setClaim(GRANT_TYPES_CLAIM, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim(RESPONSE_TYPES_CLAIM, RESPONSE_TYPES);
        claims.setClaim(TLS_CLIENT_AUTH_DN_CLAIM, getSubjectDomainName(authMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()));
        claims.setClaim(REQUEST_OBJECT_SIGNING_ALG_CLAIM, SIGNATURE_ALGORITHM);

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim(SCOPE_CLAIM, tokenScope.getRegistrationScope());
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, SIGNATURE_ALGORITHM);
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(SOFTWARE_ID_CLAIM, softwareId);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(SIGNATURE_ALGORITHM);
        jws.setKeyIdHeaderValue(authMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }

    private Object getSubjectDomainName(String certificatePem) throws CertificateException {
        X509Certificate x509Certificate = KeyUtil.createCertificateFromPemFormat(certificatePem);
        return x509Certificate.getSubjectDN().getName();
    }
}
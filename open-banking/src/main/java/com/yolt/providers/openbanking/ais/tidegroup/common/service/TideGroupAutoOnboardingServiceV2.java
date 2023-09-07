package com.yolt.providers.openbanking.ais.tidegroup.common.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.tidegroup.common.TideGroupPropertiesV2;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.util.*;

import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.CODE;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.ID_TOKEN;
import static com.yolt.providers.openbanking.ais.tidegroup.common.auth.TideGroupAuthMeansMapperV3.*;

@RequiredArgsConstructor
public class TideGroupAutoOnboardingServiceV2 {

    private static final String REDIRECT_URIS_CLAIM = "redirect_uris";
    private static final String TOKEN_ENDPOINT_AUTH_METHODS_CLAIM = "token_endpoint_auth_method";
    private static final String PRIVATE_KEY_JWT = "private_key_jwt";
    private static final String GRANT_TYPES_CLAIM = "grant_types";
    private static final String SOFTWARE_STATEMENT_CLAIM = "software_statement";
    private static final String SOFTWARE_ID_CLAIM = "software_id";
    private static final String SCOPE_CLAIM = "scope";
    private static final String RESPONSE_TYPES_CLAIM = "response_types";
    private static final String APPLICATION_TYPE_CLAIM = "application_type";
    private static final String APPLICATION_TYPE = "web";
    private static final String ID_TOKEN_SIGNING_ALG_CLAIM = "id_token_signing_alg";
    private static final String REQUEST_OBJECT_SIGNING_CLAIM = "request_object_signing_alg";
    private static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token_endpoint_auth_signing_alg";
    private static final String ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM = "id_token_signed_response_alg";
    private static final String TYP_HEADER = "typ";
    private static final String JWT = "JWT";
    private static final int EXPIRATION_TIME_MINUTES_CLAIM = 60;

    private final TideGroupRestClientV2 restClient;
    private final TideGroupPropertiesV2 properties;

    @NonNull
    public Optional<AutoOnboardingResponse> register(HttpClient httpClient,
                                                     UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        if (urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(CLIENT_ID_NAME)) {
            return Optional.empty();
        }
        return Optional.ofNullable(invokeRegistration(httpClient, urlAutoOnboardingRequest));
    }

    @NonNull
    private AutoOnboardingResponse invokeRegistration(HttpClient httpClient,
                                                      UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        String signingKeyId = authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue();

        JsonWebSignature jws = createJws(authenticationMeans, urlAutoOnboardingRequest);
        String payload = urlAutoOnboardingRequest.getSigner().sign(jws, UUID.fromString(signingKeyId), SignatureAlgorithm.SHA256_WITH_RSA_PSS).getCompactSerialization();

        return restClient.register(httpClient, payload, properties.getRegistrationUrl());
    }

    private JsonWebSignature createJws(Map<String, BasicAuthenticationMean> authenticationMeans,
                                       UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        JwtClaims claims = new JwtClaims();
        claims.setAudience(properties.getRegistrationAudience());
        claims.setIssuer(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(EXPIRATION_TIME_MINUTES_CLAIM);
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());

        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHODS_CLAIM, PRIVATE_KEY_JWT);
        claims.setClaim(GRANT_TYPES_CLAIM, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim(RESPONSE_TYPES_CLAIM, Collections.singletonList(CODE + " " + ID_TOKEN));
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, authenticationMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(SOFTWARE_ID_CLAIM, authenticationMeans.get(SOFTWARE_ID_NAME).getValue());

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim(SCOPE_CLAIM, tokenScope.getRegistrationScope());
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNING_ALG_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(REQUEST_OBJECT_SIGNING_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(TOKEN_ENDPOINT_AUTH_SIGNING_ALG, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        jws.setKeyIdHeaderValue(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }
}
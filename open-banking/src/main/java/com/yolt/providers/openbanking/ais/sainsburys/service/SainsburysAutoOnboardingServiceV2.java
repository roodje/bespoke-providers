package com.yolt.providers.openbanking.ais.sainsburys.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.TokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.AuthenticationService;
import com.yolt.providers.openbanking.ais.sainsburys.SainsburysPropertiesV2;
import com.yolt.providers.openbanking.ais.sainsburys.service.ais.restclient.SainsburysRestClientV2;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.util.*;

import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.CODE;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.ID_TOKEN;
import static com.yolt.providers.openbanking.ais.sainsburys.auth.SainsburysAuthMeansMapperV2.*;

@RequiredArgsConstructor
public class SainsburysAutoOnboardingServiceV2 {

    private final SainsburysRestClientV2 restClient;
    private final String signingAlgorithm;
    private final SainsburysPropertiesV2 properties;
    private final AuthenticationService authenticationService;
    private final TokenScope scope;

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


    @NonNull
    public Optional<AutoOnboardingResponse> register(HttpClient httpClient,
                                                     UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        if (urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(CLIENT_ID_NAME)) {
            return Optional.empty();
        }
        return Optional.of(invokeRegistration(httpClient,
                urlAutoOnboardingRequest.getAuthenticationMeans(),
                urlAutoOnboardingRequest,
                urlAutoOnboardingRequest.getSigner()));
    }

    @NonNull
    protected AutoOnboardingResponse invokeRegistration(final HttpClient httpClient,
                                                        final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                        final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                        final Signer signer) throws TokenInvalidException {
        String signingKeyId = authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue();

        JsonWebSignature jws = createJws(authenticationMeans, urlAutoOnboardingRequest);
        String payload = signer.sign(jws, UUID.fromString(signingKeyId), SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(signingAlgorithm)).getCompactSerialization();

        return restClient.register(httpClient, payload, properties.getRegistrationUrl());
    }

    public void removeRegistration(final HttpClient httpClient,
                                   final DefaultAuthMeans authMeans,
                                   final Signer signer) throws TokenInvalidException {
        AccessMeans clientAccessMeans = authenticationService.getClientAccessTokenWithoutCache(httpClient, authMeans, scope, signer);
        restClient.removeRegistration(httpClient, properties.getRegistrationUrl() + "/{clientId}", authMeans.getClientId(), clientAccessMeans);
    }

    protected JsonWebSignature createJws(Map<String, BasicAuthenticationMean> authenticationMeans,
                                         UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        String softwareId = authenticationMeans.get(SOFTWARE_ID_NAME).getValue();

        JwtClaims claims = new JwtClaims();
        claims.setAudience(properties.getAudience());
        claims.setIssuer(softwareId);
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());

        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHODS_CLAIM, PRIVATE_KEY_JWT);
        claims.setClaim(GRANT_TYPES_CLAIM, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim(RESPONSE_TYPES_CLAIM, Collections.singletonList(CODE + " " + ID_TOKEN));
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, authenticationMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(SOFTWARE_ID_CLAIM, softwareId);

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim(SCOPE_CLAIM, tokenScope.getRegistrationScope());
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNING_ALG_CLAIM, signingAlgorithm);
        claims.setClaim(REQUEST_OBJECT_SIGNING_CLAIM, signingAlgorithm);
        claims.setClaim(TOKEN_ENDPOINT_AUTH_SIGNING_ALG, signingAlgorithm);
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, signingAlgorithm);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(signingAlgorithm);
        jws.setKeyIdHeaderValue(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }
}
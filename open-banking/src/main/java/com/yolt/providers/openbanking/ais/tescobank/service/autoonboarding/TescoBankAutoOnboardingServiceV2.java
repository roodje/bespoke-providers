package com.yolt.providers.openbanking.ais.tescobank.service.autoonboarding;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.tescobank.TescoBankPropertiesV2;
import com.yolt.providers.openbanking.ais.tescobank.service.restclient.TescoBankRestClientV2;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.util.*;

import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;
import static com.yolt.providers.openbanking.ais.tescobank.auth.TescoBankAuthMeansBuilderV3.*;

@RequiredArgsConstructor
public class TescoBankAutoOnboardingServiceV2 {

    private static final String REDIRECT_URIS_CLAIM = "redirect_uris";
    private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLAIM = "token_endpoint_auth_method";
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

    private final TescoBankRestClientV2 restClient;
    private final String signatureAlgorithm;
    private final String authMethod;
    private final TescoBankPropertiesV2 properties;

    public Optional<AutoOnboardingResponse> register(HttpClient httpClient,
                                                     UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        if (isRegistrationRequestValid(urlAutoOnboardingRequest)) {
            return Optional.ofNullable(invokeRegistration(httpClient, urlAutoOnboardingRequest));
        }
        return Optional.empty();
    }

    protected AutoOnboardingResponse invokeRegistration(HttpClient httpClient,
                                                        UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        Signer signer = urlAutoOnboardingRequest.getSigner();
        Map<String, BasicAuthenticationMean> authMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        JsonWebSignature jws = createJws(authMeans, urlAutoOnboardingRequest);
        String payload = signer.sign(jws, UUID.fromString(authMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()), SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(signatureAlgorithm))
                .getCompactSerialization();

        return restClient.register(httpClient, payload, properties.getRegistrationUrl());
    }

    protected JsonWebSignature createJws(Map<String, BasicAuthenticationMean> authMeans,
                                         UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        String softwareId = authMeans.get(SOFTWARE_ID_NAME).getValue();

        JwtClaims claims = new JwtClaims();
        claims.setAudience(properties.getAudience());
        claims.setIssuer(softwareId);
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(EXPIRATION_TIME_MINUTES_IN_THE_FUTURE);
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());

        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM, authMethod);
        claims.setClaim(GRANT_TYPES_CLAIM, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim(RESPONSE_TYPES_CLAIM, RESPONSE_TYPES);
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, authMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(SOFTWARE_ID_CLAIM, softwareId);

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim(SCOPE_CLAIM, tokenScope.getRegistrationScope());
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(REQUEST_OBJECT_SIGNING_ALG_CLAIM, signatureAlgorithm);
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, signatureAlgorithm);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(signatureAlgorithm);
        jws.setKeyIdHeaderValue(authMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }

    private boolean isRegistrationRequestValid(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        return RegistrationOperation.UPDATE.equals(urlAutoOnboardingRequest.getRegistrationOperation())
                || (RegistrationOperation.CREATE.equals(urlAutoOnboardingRequest.getRegistrationOperation())
                && !urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(CLIENT_ID_NAME));
    }
}
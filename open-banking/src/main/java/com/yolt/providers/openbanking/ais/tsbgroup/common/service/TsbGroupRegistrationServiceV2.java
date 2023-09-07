package com.yolt.providers.openbanking.ais.tsbgroup.common.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.tsbgroup.common.auth.TsbGroupAuthMeansBuilderV3;
import com.yolt.providers.openbanking.ais.tsbgroup.common.config.TsbGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.tsbgroup.common.service.restclient.TsbGroupRegistrationRestClientV2;
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
import static com.yolt.providers.openbanking.ais.tsbgroup.common.auth.TsbGroupAuthMeansBuilderV3.*;

@RequiredArgsConstructor
public class TsbGroupRegistrationServiceV2 {

    private final TsbGroupRegistrationRestClientV2 restClient;
    private final TsbGroupPropertiesV2 properties;

    private static final String SIGNATURE_ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;

    @NonNull
    public Optional<AutoOnboardingResponse> register(HttpClient httpClient,
                                                     UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        if (!isRegistrationRequestValid(urlAutoOnboardingRequest)) {
            return Optional.empty();
        }
        return Optional.ofNullable(invokeRegistration(httpClient,
                urlAutoOnboardingRequest.getAuthenticationMeans(),
                urlAutoOnboardingRequest));
    }

    @NonNull
    private AutoOnboardingResponse invokeRegistration(HttpClient httpClient,
                                                      Map<String, BasicAuthenticationMean> authenticationMeans,
                                                      UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        String signingKeyId = authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue();
        JsonWebSignature jws = createJws(authenticationMeans, urlAutoOnboardingRequest);
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(SIGNATURE_ALGORITHM);
        String payload = urlAutoOnboardingRequest.getSigner().sign(jws, UUID.fromString(signingKeyId), signatureAlgorithm).getCompactSerialization();

        return restClient.register(httpClient, payload, properties.getRegistrationUrl());
    }

    private JsonWebSignature createJws(Map<String, BasicAuthenticationMean> authenticationMeans,
                                       UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        JwtClaims claims = new JwtClaims();
        claims.setAudience(authenticationMeans.get(INSTITUTION_ID_NAME).getValue());
        claims.setIssuer(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setJwtId(UUID.randomUUID().toString().toLowerCase());

        claims.setClaim("redirect_uris", urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim("token_endpoint_auth_method", "client_secret_post");
        claims.setClaim("grant_types", Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim("response_types", Collections.singletonList(CODE + " " + ID_TOKEN));
        claims.setClaim("software_statement", authenticationMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim("software_id", authenticationMeans.get(SOFTWARE_ID_NAME).getValue());

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim("scope", tokenScope.getRegistrationScope());
        claims.setClaim("application_type", "web");
        claims.setClaim("token_endpoint_auth_signing_alg", SIGNATURE_ALGORITHM);
        claims.setClaim("request_object_signing_alg", SIGNATURE_ALGORITHM);
        claims.setClaim("id_token_signed_response_alg", SIGNATURE_ALGORITHM);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(SIGNATURE_ALGORITHM);
        jws.setKeyIdHeaderValue(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader("typ", "JWT");
        return jws;
    }

    private boolean isRegistrationRequestValid(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        return RegistrationOperation.UPDATE.equals(urlAutoOnboardingRequest.getRegistrationOperation()) ||
                (RegistrationOperation.CREATE.equals(urlAutoOnboardingRequest.getRegistrationOperation()) &&
                        !urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(TsbGroupAuthMeansBuilderV3.CLIENT_ID_NAME));
    }
}
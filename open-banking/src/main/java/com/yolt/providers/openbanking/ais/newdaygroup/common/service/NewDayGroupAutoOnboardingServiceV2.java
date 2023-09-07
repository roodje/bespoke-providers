package com.yolt.providers.openbanking.ais.newdaygroup.common.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.newdaygroup.common.auth.NewDayGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.newdaygroup.common.http.NewDayGroupRestClientV2;
import com.yolt.providers.openbanking.ais.newdaygroup.common.model.NewDayAutoOnboardingResponse;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;

@RequiredArgsConstructor
public class NewDayGroupAutoOnboardingServiceV2 {

    private final NewDayGroupRestClientV2 restClient;
    private final DefaultProperties properties;

    private static final String APPLICATION_TYPE = "web";
    private static final String ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final String TOKEN_ENDPOINT_AUTH_METHOD_CLAIM = "token_endpoint_auth_method";
    private static final String GRANT_TYPES_CLAIM = "grant_types";
    private static final String REDIRECT_URIS_CLAIM = "redirect_uris";
    private static final String RESPONSE_TYPES_CLAIM = "response_types";
    private static final String SOFTWARE_ID_CLAIM = "software_id";
    private static final String SCOPE_CLAIM = "scope";
    private static final String SSA_CLAIM = "software_statement";
    private static final String APPLICATION_TYPE_CLAIM = "application_type";
    private static final String ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM = "id_token_signed_response_alg";
    private static final String REQUEST_OBJECT_SIGNING_ALG_CLAIM = "request_object_signing_alg";
    private static final String TYP_HEADER = "typ";
    private static final String JWT = "JWT";
    private static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG_CLAIM = "token_endpoint_auth_signing_alg";
    private static final String CLIENT_SECRET_BASIC = "client_secret_basic";
    private static final String CODE_ID_TOKEN = "code id_token";

    @NonNull
    public Optional<NewDayAutoOnboardingResponse> register(final RestTemplate restTemplate,
                                                           @NonNull final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                           @NonNull final String providerKey) {
        if (urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(NewDayGroupAuthMeansBuilderV2.CLIENT_ID_NAME)) {
            return Optional.empty();
        }
        return invokeRegistration(restTemplate, urlAutoOnboardingRequest, providerKey);
    }

    @NonNull
    private Optional<NewDayAutoOnboardingResponse> invokeRegistration(final RestTemplate restTemplate,
                                                                      @NonNull final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                                      @NonNull final String providerKey) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        String signingKeyId = authenticationMeans.get(NewDayGroupAuthMeansBuilderV2.SIGNING_KEY_ID).getValue();

        JsonWebSignature jws = createJws(authenticationMeans, urlAutoOnboardingRequest);

        Signer signer = urlAutoOnboardingRequest.getSigner();
        String payload = signer.sign(jws, UUID.fromString(signingKeyId), SignatureAlgorithm.SHA256_WITH_RSA_PSS)
                .getCompactSerialization();

        return restClient.register(restTemplate, payload, providerKey);
    }

    private JsonWebSignature createJws(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                       final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(authenticationMeans.get(NewDayGroupAuthMeansBuilderV2.SOFTWARE_ID_NAME).getValue());
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setAudience(properties.getBaseUrl());
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());
        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM, CLIENT_SECRET_BASIC);
        claims.setClaim(GRANT_TYPES_CLAIM, List.of(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim(RESPONSE_TYPES_CLAIM, CODE_ID_TOKEN);
        claims.setClaim(SOFTWARE_ID_CLAIM, authenticationMeans.get(NewDayGroupAuthMeansBuilderV2.SOFTWARE_ID_NAME).getValue());

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim(SCOPE_CLAIM, tokenScope.getRegistrationScope());
        claims.setClaim(SSA_CLAIM, authenticationMeans.get(NewDayGroupAuthMeansBuilderV2.SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, ALGORITHM);
        claims.setClaim(REQUEST_OBJECT_SIGNING_ALG_CLAIM, ALGORITHM);
        claims.setClaim(TOKEN_ENDPOINT_AUTH_SIGNING_ALG_CLAIM, ALGORITHM);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(ALGORITHM);
        jws.setKeyIdHeaderValue(authenticationMeans.get(NewDayGroupAuthMeansBuilderV2.SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }
}
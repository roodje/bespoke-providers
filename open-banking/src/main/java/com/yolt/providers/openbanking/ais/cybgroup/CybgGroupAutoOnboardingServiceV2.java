package com.yolt.providers.openbanking.ais.cybgroup;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.openbanking.ais.cybgroup.common.auth.CybgGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.config.CybgGroupPropertiesV2;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupClientRegistration;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.CODE;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.ID_TOKEN;

@RequiredArgsConstructor
public class CybgGroupAutoOnboardingServiceV2 {

    private static final String APPLICATION_TYPE = "web";
    private static final String ALGORITHM = "RS256";
    private static final String TYP = "JWT";

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

    private static final String APPLICATION_JOSE = "application/jose";
    private static final String CLIENT_SECRET_BASIC = "client_secret_basic";
    private final CybgGroupPropertiesV2 properties;

    @NonNull
    public Optional<CybgGroupClientRegistration> register(@NonNull final RestTemplate restTemplate,
                                                          @NonNull final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                          @NonNull final String providerKey,
                                                          @NonNull final CybgGroupPropertiesV2 properties) {
        if (urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(CybgGroupAuthMeansBuilderV2.CLIENT_ID_NAME)) {
            return Optional.empty();
        }
        return invokeRegistration(restTemplate, urlAutoOnboardingRequest, providerKey, properties);
    }

    @NonNull
    private Optional<CybgGroupClientRegistration> invokeRegistration(@NonNull final RestTemplate restTemplate,
                                                                     @NonNull final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                                     @NonNull final String providerKey,
                                                                     @NonNull final CybgGroupPropertiesV2 properties) {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        String signingKeyId = authenticationMeans.get(CybgGroupAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME).getValue();

        JsonWebSignature jws = createJws(authenticationMeans, urlAutoOnboardingRequest);

        Signer signer = urlAutoOnboardingRequest.getSigner();
        String payload = signer.sign(jws, UUID.fromString(signingKeyId), SignatureAlgorithm.SHA256_WITH_RSA).getCompactSerialization();

        return exchange(restTemplate, payload, providerKey, properties.getRegistrationUrl());
    }

    @NonNull
    private Optional<CybgGroupClientRegistration> exchange(@NonNull final RestTemplate restTemplate,
                                                           @NonNull final String payload,
                                                           @NonNull final String providerKey,
                                                           @NonNull final String registrationUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_TYPE, APPLICATION_JOSE);
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<CybgGroupClientRegistration> responseEntity = restTemplate.exchange(
                    registrationUrl, HttpMethod.POST, httpEntity, CybgGroupClientRegistration.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            throw new AutoOnboardingException(providerKey, "Auto-Onboarding failed", e);
        }
    }

    private JsonWebSignature createJws(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                       final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        JwtClaims claims = new JwtClaims();
        claims.setIssuer(authenticationMeans.get(CybgGroupAuthMeansBuilderV2.SOFTWARE_ID_NAME).getValue());
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setAudience(properties.getAudience());
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());
        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM, CLIENT_SECRET_BASIC);

        claims.setClaim(GRANT_TYPES_CLAIM, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim(RESPONSE_TYPES_CLAIM, createResponseTypesString());
        claims.setClaim(SOFTWARE_ID_CLAIM, authenticationMeans.get(CybgGroupAuthMeansBuilderV2.SOFTWARE_ID_NAME).getValue());

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim(SCOPE_CLAIM, tokenScope.getRegistrationScope());

        claims.setClaim(SSA_CLAIM, authenticationMeans.get(CybgGroupAuthMeansBuilderV2.SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, ALGORITHM);
        claims.setClaim(REQUEST_OBJECT_SIGNING_ALG_CLAIM, ALGORITHM);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        jws.setKeyIdHeaderValue(authenticationMeans.get(CybgGroupAuthMeansBuilderV2.SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, TYP);
        return jws;
    }

    private String createResponseTypesString() {
        return CODE + " " + ID_TOKEN;
    }
}


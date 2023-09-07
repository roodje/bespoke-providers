package com.yolt.providers.openbanking.ais.vanquisgroup;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.auth.VanquisGroupAuthMeansBuilderV2;
import com.yolt.providers.openbanking.ais.vanquisgroup.common.properties.VanquisGroupPropertiesV2;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.CODE;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.ID_TOKEN;

@Service
@RequiredArgsConstructor
public class VanquisAutoOnboardingServiceV2 {

    private static final String REDIRECT_URIS_CLAIM = "redirect_uris";
    private static final String TOKEN_ENDPOINT_AUTH_METHODS_CLAIM = "token_endpoint_auth_methods";
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
    private static final String TYP_HEADER = "typ";
    private static final String JWT = "JWT";

    @NonNull
    public Optional<AutoOnboardingResponse> register(@NonNull final RestTemplate restTemplate,
                                                     @NonNull final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                     @NonNull final String providerKey,
                                                     @NonNull final VanquisGroupPropertiesV2 properties) {
        if (urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(VanquisGroupAuthMeansBuilderV2.CLIENT_ID_NAME)) {
            return Optional.empty();
        }
        return invokeRegistration(restTemplate,
                urlAutoOnboardingRequest.getAuthenticationMeans(),
                urlAutoOnboardingRequest,
                urlAutoOnboardingRequest.getSigner(),
                providerKey,
                properties);
    }

    @NonNull
    private Optional<AutoOnboardingResponse> invokeRegistration(@NonNull final RestTemplate restTemplate,
                                                                @NonNull final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                @NonNull final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                                                @NonNull final Signer signer,
                                                                @NonNull final String providerKey,
                                                                @NonNull final VanquisGroupPropertiesV2 properties) {
        String signingKeyId = authenticationMeans.get(VanquisGroupAuthMeansBuilderV2.SIGNING_PRIVATE_KEY_ID_NAME).getValue();

        JsonWebSignature jws = createJws(authenticationMeans, properties, urlAutoOnboardingRequest);
        String payload = signer.sign(jws, UUID.fromString(signingKeyId), SignatureAlgorithm.SHA256_WITH_RSA_PSS).getCompactSerialization();

        return exchange(restTemplate, payload, providerKey, properties.getRegistrationUrl());
    }

    private Optional<AutoOnboardingResponse> exchange(@NonNull final RestTemplate restTemplate,
                                                      @NonNull final String payload,
                                                      @NonNull final String providerKey,
                                                      @NonNull final String registrationUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        headers.set(HttpHeaders.CONTENT_TYPE, "application/jwt");
        HttpEntity<String> httpEntity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<AutoOnboardingResponse> responseEntity = restTemplate.exchange(registrationUrl, HttpMethod.POST, httpEntity, AutoOnboardingResponse.class);
            return Optional.ofNullable(responseEntity.getBody());
        } catch (HttpClientErrorException e) {
            throw new AutoOnboardingException(providerKey, "Auto-Onboarding failed", e);
        }
    }

    private JsonWebSignature createJws(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                       final VanquisGroupPropertiesV2 properties,
                                       final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        JwtClaims claims = new JwtClaims();
        claims.setAudience(properties.getRegistrationAudience());
        claims.setIssuer(authenticationMeans.get(VanquisGroupAuthMeansBuilderV2.SOFTWARE_ID_NAME).getValue());
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());

        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHODS_CLAIM, PRIVATE_KEY_JWT);
        claims.setClaim(GRANT_TYPES_CLAIM, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim(RESPONSE_TYPES_CLAIM, Collections.singletonList(CODE + " " + ID_TOKEN));
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, authenticationMeans.get(VanquisGroupAuthMeansBuilderV2.SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(SOFTWARE_ID_CLAIM, authenticationMeans.get(VanquisGroupAuthMeansBuilderV2.SOFTWARE_ID_NAME).getValue());
        claims.setClaim(SCOPE_CLAIM, getRegistrationScope(urlAutoOnboardingRequest.getScopes()));
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNING_ALG_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(REQUEST_OBJECT_SIGNING_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(TOKEN_ENDPOINT_AUTH_SIGNING_ALG, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        jws.setKeyIdHeaderValue(authenticationMeans.get(VanquisGroupAuthMeansBuilderV2.SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }
    
    private String getRegistrationScope(Set<TokenScope> tokenScopes) {
        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(tokenScopes);
        return tokenScope.getRegistrationScope() + " offline_access";
    }
}
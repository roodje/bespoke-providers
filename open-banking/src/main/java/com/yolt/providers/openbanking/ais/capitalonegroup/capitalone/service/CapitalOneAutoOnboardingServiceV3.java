package com.yolt.providers.openbanking.ais.capitalonegroup.capitalone.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.capitalonegroup.capitalone.config.CapitalOnePropertiesV2;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.model.CapitalOneDynamicRegistrationResponse;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.service.ais.restclient.CapitalOneGroupRestClientV2;
import com.yolt.providers.openbanking.ais.common.enums.GrantTypes;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.util.*;

import static com.yolt.providers.openbanking.ais.capitalonegroup.common.auth.CapitalOneAuthMeansBuilderV3.*;
import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.CODE;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.ID_TOKEN;

@RequiredArgsConstructor
public class CapitalOneAutoOnboardingServiceV3 {

    private final CapitalOneGroupRestClientV2 restClient;
    private final CapitalOnePropertiesV2 properties;

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
    private static final String ID_TOKEN_SIGNING_ALG_CLAIM = "id_token_signed_response_alg";
    private static final String REQUEST_OBJECT_SIGNING_CLAIM = "request_object_signing_alg";
    private static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token_endpoint_auth_signing_alg_values_supported";
    private static final String TYP_HEADER = "typ";
    private static final String JWT = "JWT";
    private static final List<GrantTypes> GRANT_TYPES = Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN);

    @NonNull
    public Optional<CapitalOneDynamicRegistrationResponse> register(final HttpClient httpClient,
                                                                    @NonNull final UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        if (urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(CLIENT_ID_NAME)) {
            return Optional.empty();
        }
        return invokeRegistration(httpClient, urlAutoOnboardingRequest);
    }

    @NonNull
    private Optional<CapitalOneDynamicRegistrationResponse> invokeRegistration(@NonNull final HttpClient httpClient,
                                                                               @NonNull final UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        String signingKeyId = authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue();

        JsonWebSignature jws = createJws(authenticationMeans, urlAutoOnboardingRequest);

        Signer signer = urlAutoOnboardingRequest.getSigner();
        String payload = signer.sign(jws, UUID.fromString(signingKeyId), SignatureAlgorithm.SHA256_WITH_RSA_PSS).getCompactSerialization();

        return restClient.register(httpClient, payload, properties.getBaseUrl() + "/oauth/register");
    }

    private JsonWebSignature createJws(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                       final UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        JwtClaims claims = new JwtClaims();
        claims.setAudience(authenticationMeans.get(INSTITUTION_ID_NAME).getValue());
        claims.setIssuer(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());

        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHODS_CLAIM, PRIVATE_KEY_JWT);
        claims.setClaim(GRANT_TYPES_CLAIM, GRANT_TYPES);
        claims.setClaim(RESPONSE_TYPES_CLAIM, Collections.singletonList(CODE + " " + ID_TOKEN));
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, authenticationMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(SOFTWARE_ID_CLAIM, authenticationMeans.get(SOFTWARE_ID_NAME).getValue());

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim(SCOPE_CLAIM, tokenScope.getRegistrationScope());
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNING_ALG_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(REQUEST_OBJECT_SIGNING_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(TOKEN_ENDPOINT_AUTH_SIGNING_ALG, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        jws.setKeyIdHeaderValue(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }
}
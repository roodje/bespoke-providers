package com.yolt.providers.openbanking.ais.virginmoney2group.common.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.common.enums.GrantTypes;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.virginmoney2group.common.properties.VirginMoney2GroupProperties;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;
import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.CODE;
import static com.yolt.providers.openbanking.ais.common.enums.ResponseTypes.ID_TOKEN;
import static com.yolt.providers.openbanking.ais.virginmoney2group.common.auth.VirginMoney2GroupAuthMeansBuilder.*;

@RequiredArgsConstructor
public class VirginMoney2GroupAutoOnboardingService {

    private static final String REDIRECT_URIS_CLAIM = "redirect_uris";
    private static final String TOKEN_ENDPOINT_AUTH_METHODS_CLAIM = "token_endpoint_auth_method";
    private static final String CLIENT_SECRET_BASIC = "client_secret_basic";
    private static final String GRANT_TYPES_CLAIM = "grant_types";
    private static final String SOFTWARE_STATEMENT_CLAIM = "software_statement";
    private static final String TLS_CLIENT_AUTH_DN_CLAIM = "tls_client_auth_dn";
    private static final String SOFTWARE_ID_CLAIM = "software_id";
    private static final String SCOPE_CLAIM = "scope";
    private static final String RESPONSE_TYPES_CLAIM = "response_types";
    private static final String APPLICATION_TYPE_CLAIM = "application_type";
    private static final String APPLICATION_TYPE = "web";
    private static final String ID_TOKEN_SIGNING_ALG_CLAIM = "id_token_signing_alg";
    private static final String ID_TOKEN_SIGNING_RESPONSE_ALG_CLAIM = "id_token_signed_response_alg";
    private static final String REQUEST_OBJECT_SIGNING_CLAIM = "request_object_signing_alg";
    private static final String TOKEN_ENDPOINT_AUTH_SIGNING_ALG = "token_endpoint_auth_signing_alg";
    private static final String TYP_HEADER = "typ";
    private static final String JWT = "JWT";
    private static final List<GrantTypes> GRANT_TYPES = Arrays.asList(AUTHORIZATION_CODE, REFRESH_TOKEN, CLIENT_CREDENTIALS);
    private static final String ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final List<String> AUTO_ON_BOARDING_UNNECESSARY_MEANS = Arrays.asList(CLIENT_ID_NAME, CLIENT_SECRET_NAME);

    private final VirginMoney2GroupProperties properties;
    private final HttpClientFactory httpClientFactory;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final ProviderIdentification providerIdentification;

    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans(Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap) {
        return typedAuthenticationMeansMap.entrySet()
                .stream()
                .filter(entry -> AUTO_ON_BOARDING_UNNECESSARY_MEANS.contains(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, BasicAuthenticationMean> register(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(urlAutoOnboardingRequest.getAuthenticationMeans());
        if (!urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(CLIENT_ID_NAME)) {
            try {
                invokeRegistration(urlAutoOnboardingRequest)
                        .ifPresent(response -> {
                            BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                    response.getClientId());
                            BasicAuthenticationMean clientSecretMean = new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(),
                                    response.getClientSecret());
                            mutableMeans.put(CLIENT_ID_NAME, clientIdMean);
                            mutableMeans.put(CLIENT_SECRET_NAME, clientSecretMean);
                        });
            } catch (RestClientException | IllegalStateException | TokenInvalidException | CertificateException e) {
                throw new AutoOnboardingException(providerIdentification.getIdentifier(), "Auto-onboarding failed for Virgin Money Bank", e);
            }
        }

        return mutableMeans;
    }

    private Optional<AutoOnboardingResponse> invokeRegistration(UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws CertificateException, TokenInvalidException {

        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        DefaultAuthMeans authMeans = getAuthenticationMeans.apply(authenticationMeans);
        String signingKeyId = authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue();

        JsonWebSignature jws = createJws(authenticationMeans, urlAutoOnboardingRequest);
        String payload = urlAutoOnboardingRequest.getSigner().sign(jws, UUID.fromString(signingKeyId), SignatureAlgorithm.SHA256_WITH_RSA_PSS).getCompactSerialization();

        return exchange(urlAutoOnboardingRequest.getRestTemplateManager(),
                authMeans,
                payload);
    }

    private Optional<AutoOnboardingResponse> exchange(RestTemplateManager restTemplateManager,
                                                      DefaultAuthMeans authMeans,
                                                      String payload) throws TokenInvalidException {
        HttpClient httpClient = httpClientFactory.createHttpClient(restTemplateManager, authMeans, providerIdentification.getDisplayName());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set(HttpHeaders.CONTENT_TYPE, "application/jose");

        ResponseEntity<AutoOnboardingResponse> responseEntity = httpClient.exchange(
                properties.getRegistrationUrl(),
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                ProviderClientEndpoints.REGISTER,
                AutoOnboardingResponse.class);
        return Optional.ofNullable(responseEntity.getBody());
    }

    protected JsonWebSignature createJws(Map<String, BasicAuthenticationMean> authenticationMeans,
                                         UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws CertificateException {
        JwtClaims claims = new JwtClaims();
        claims.setAudience(properties.getRegistrationAudience());
        claims.setIssuer(authenticationMeans.get(SOFTWARE_ID_NAME).getValue());
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());

        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHODS_CLAIM, CLIENT_SECRET_BASIC);
        claims.setClaim(GRANT_TYPES_CLAIM, GRANT_TYPES);
        claims.setClaim(RESPONSE_TYPES_CLAIM, Collections.singletonList(CODE + " " + ID_TOKEN));
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, authenticationMeans.get(SOFTWARE_STATEMENT_ASSERTION_NAME).getValue());
        claims.setClaim(SOFTWARE_ID_CLAIM, authenticationMeans.get(SOFTWARE_ID_NAME).getValue());

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim(SCOPE_CLAIM, tokenScope.getRegistrationScope());
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNING_ALG_CLAIM, ALGORITHM);
        claims.setClaim(ID_TOKEN_SIGNING_RESPONSE_ALG_CLAIM, ALGORITHM);
        claims.setClaim(REQUEST_OBJECT_SIGNING_CLAIM, ALGORITHM);
        claims.setClaim(TOKEN_ENDPOINT_AUTH_SIGNING_ALG, ALGORITHM);

        claims.setClaim(TLS_CLIENT_AUTH_DN_CLAIM, getSubjectDomainName(authenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue()));

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(ALGORITHM);
        jws.setKeyIdHeaderValue(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }

    private Object getSubjectDomainName(String certificatePem) throws CertificateException {
        X509Certificate x509Certificate = KeyUtil.createCertificateFromPemFormat(certificatePem);
        return x509Certificate.getSubjectDN().getName();
    }
}

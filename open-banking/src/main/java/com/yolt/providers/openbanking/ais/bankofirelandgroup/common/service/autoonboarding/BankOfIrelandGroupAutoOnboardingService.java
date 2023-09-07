package com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.autoonboarding;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.autoonboarding.RegistrationOperation;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.BankOfIrelandGroupProperties;
import com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.restclient.BankOfIrelandGroupRestClient;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;

import java.security.cert.CertificateException;
import java.util.*;

import static com.yolt.providers.openbanking.ais.common.enums.GrantTypes.*;

@RequiredArgsConstructor
public abstract class BankOfIrelandGroupAutoOnboardingService {

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

    private final BankOfIrelandGroupRestClient restClient;
    private final String signatureAlgorithm;
    private final String authMethod;
    private final BankOfIrelandGroupProperties properties;

    public Optional<AutoOnboardingResponse> register(HttpClient httpClient,
                                                     UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException, CertificateException {
        if (isRegistrationRequestValid(urlAutoOnboardingRequest)) {
            return Optional.ofNullable(invokeRegistration(httpClient, urlAutoOnboardingRequest));
        }
        return Optional.empty();
    }

    protected AutoOnboardingResponse invokeRegistration(HttpClient httpClient,
                                                        UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException, CertificateException {
        Signer signer = urlAutoOnboardingRequest.getSigner();
        Map<String, BasicAuthenticationMean> authMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        JsonWebSignature jws = createJws(authMeans, urlAutoOnboardingRequest);
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.findByJsonSignatureAlgorithmOrThrowException(this.signatureAlgorithm);

        String payload = signer
                .sign(jws, getPrivateKeyId(authMeans), signatureAlgorithm)
                .getCompactSerialization();

        return restClient.register(httpClient, payload, properties.getRegistrationUrl());
    }

    protected JsonWebSignature createJws(Map<String, BasicAuthenticationMean> authMeans,
                                         UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws CertificateException {
        String softwareId = getSoftwareId(authMeans);

        JwtClaims claims = new JwtClaims();
        claims.setAudience(properties.getAudience());
        claims.setIssuer(softwareId);
        claims.setIssuedAtToNow();
        claims.setExpirationTimeMinutesInTheFuture(EXPIRATION_TIME_MINUTES_IN_THE_FUTURE);
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());

        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM, authMethod);
        claims.setClaim(TLS_CLIENT_AUTH_DN_CLAIM, getSubjectDomainName(authMeans));
        claims.setClaim(GRANT_TYPES_CLAIM, Arrays.asList(AUTHORIZATION_CODE, CLIENT_CREDENTIALS, REFRESH_TOKEN));
        claims.setClaim(RESPONSE_TYPES_CLAIM, RESPONSE_TYPES);
        claims.setClaim(SOFTWARE_STATEMENT_CLAIM, getSoftwareStatementAssertion(authMeans));
        claims.setClaim(SOFTWARE_ID_CLAIM, softwareId);

        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(urlAutoOnboardingRequest.getScopes());
        claims.setClaim(SCOPE_CLAIM, tokenScope.getRegistrationScope());
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(REQUEST_OBJECT_SIGNING_ALG_CLAIM, signatureAlgorithm);
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, signatureAlgorithm);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(signatureAlgorithm);
        jws.setKeyIdHeaderValue(getSigningKeyHeaderId(authMeans));
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }

    private boolean isRegistrationRequestValid(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        return RegistrationOperation.UPDATE.equals(urlAutoOnboardingRequest.getRegistrationOperation()) ||
               (RegistrationOperation.CREATE.equals(urlAutoOnboardingRequest.getRegistrationOperation()) && !urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(getClientIdKey()));
    }

    protected abstract String getSoftwareId(Map<String, BasicAuthenticationMean> authMeans);

    protected abstract UUID getPrivateKeyId(Map<String, BasicAuthenticationMean> authMeans);

    protected abstract Object getSubjectDomainName(Map<String, BasicAuthenticationMean> authMeans) throws CertificateException;

    protected abstract Object getSoftwareStatementAssertion(Map<String, BasicAuthenticationMean> authMeans);

    protected abstract String getSigningKeyHeaderId(Map<String, BasicAuthenticationMean> authMeans);

    protected abstract String getClientIdKey();
}
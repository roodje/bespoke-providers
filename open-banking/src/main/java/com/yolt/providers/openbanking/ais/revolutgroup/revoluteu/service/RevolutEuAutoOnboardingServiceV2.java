package com.yolt.providers.openbanking.ais.revolutgroup.revoluteu.service;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.revolutgroup.common.RevolutPropertiesV2;
import com.yolt.providers.openbanking.ais.revolutgroup.common.service.autoonboarding.AbstractRevolutGroupAutoOnboardingServiceV2;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.Getter;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.jose4j.lang.JoseException;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Getter
public class RevolutEuAutoOnboardingServiceV2 extends AbstractRevolutGroupAutoOnboardingServiceV2 {

    protected static final String REDIRECT_URIS_CLAIM = "redirect_uris";
    protected static final String TOKEN_ENDPOINT_AUTH_METHOD_CLAIM = "token_endpoint_auth_method";
    protected static final String TLS_CLIENT_AUTH_DN_CLAIM = "tls_client_auth_dn";
    protected static final String SCOPE_CLAIM = "scope";
    protected static final String SOFTWARE_STATEMENT_CLAIM = "software_statement";
    protected static final String APPLICATION_TYPE_CLAIM = "application_type";
    protected static final String ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM = "id_token_signed_response_alg";
    protected static final String REQUEST_OBJECT_SIGNING_ALG_CLAIM = "request_object_signing_alg";

    protected static final String TOKEN_ENDPOINT_AUTH_METHOD = "tls_client_auth";
    protected static final String APPLICATION_TYPE = "web";

    private final RevolutEuSoftwareStatementGeneratorV2 softwareStatementGenerator;
    private final Clock clock;

    public RevolutEuAutoOnboardingServiceV2(RevolutPropertiesV2 properties,
                                            Clock clock,
                                            String clientIdAuthMeanName,
                                            RevolutEuSoftwareStatementGeneratorV2 softwareStatementGenerator) {
        super(properties, clientIdAuthMeanName);
        this.clock = clock;
        this.softwareStatementGenerator = softwareStatementGenerator;
    }

    @Override
    protected String createRegistrationPayload(final UrlAutoOnboardingRequest urlAutoOnboardingRequest,
                                               final DefaultAuthMeans defaultAuthMeans,
                                               final Signer signer,
                                               final String tlsClientAuthDn) throws TokenInvalidException {

        Map<String, BasicAuthenticationMean> authMeans = urlAutoOnboardingRequest.getAuthenticationMeans();

        Instant issuanceTime = clock.instant();
        Instant expirationTime = issuanceTime.plus(1, ChronoUnit.HOURS);

        JwtClaims claims = new JwtClaims();
        claims.setIssuer("notNull"); // They expect a value - more info in readme
        claims.setIssuedAt(NumericDate.fromMilliseconds(issuanceTime.toEpochMilli()));
        claims.setExpirationTime(NumericDate.fromMilliseconds(expirationTime.toEpochMilli()));
        claims.setAudience("revolut"); // They expect this value - more info in readme
        claims.setClaim(SCOPE_CLAIM, getRegistrationScopes(urlAutoOnboardingRequest.getScopes()));
        claims.setClaim(REDIRECT_URIS_CLAIM, urlAutoOnboardingRequest.getRedirectUrls());
        claims.setClaim(TOKEN_ENDPOINT_AUTH_METHOD_CLAIM, TOKEN_ENDPOINT_AUTH_METHOD);
        claims.setClaim(APPLICATION_TYPE_CLAIM, APPLICATION_TYPE);
        claims.setClaim(ID_TOKEN_SIGNED_RESPONSE_ALG_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(REQUEST_OBJECT_SIGNING_ALG_CLAIM, AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        claims.setClaim(TLS_CLIENT_AUTH_DN_CLAIM, tlsClientAuthDn);

        try {
            claims.setClaim(SOFTWARE_STATEMENT_CLAIM, softwareStatementGenerator
                    .generateSoftwareStatementClaim(authMeans, urlAutoOnboardingRequest.getRedirectUrls()));       } catch (JoseException e) {
            throw new TokenInvalidException("Cannot serialize SSA");
        }

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_PSS_USING_SHA256);
        jws.setKeyIdHeaderValue(defaultAuthMeans.getSigningKeyIdHeader());
        jws.setPayload(claims.toJson());

        return signer.sign(jws, defaultAuthMeans.getSigningPrivateKeyId(), SignatureAlgorithm.SHA256_WITH_RSA_PSS).getCompactSerialization();
    }

    protected List<String> getRegistrationScopes(Set<TokenScope> tokenScopes) {
        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(tokenScopes);
        List<String> registrationScopes = new ArrayList<>();
        Collections.addAll(registrationScopes, tokenScope.getRegistrationScope().split(" "));
        registrationScopes.add("fundsconfirmations");
        return registrationScopes;
    }

    @Override
    public void removeAutoConfiguration(HttpClient httpClient, UrlAutoOnboardingRequest urlAutoOnboardingRequest, DefaultAuthMeans defaultAuthMeans) throws TokenInvalidException {
        //TODO C4PO-9691 Temporarily do nothing - needed for tests - remove this method afterwards
    }
}

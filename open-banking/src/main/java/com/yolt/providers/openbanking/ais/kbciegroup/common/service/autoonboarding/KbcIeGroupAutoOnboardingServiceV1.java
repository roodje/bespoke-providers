package com.yolt.providers.openbanking.ais.kbciegroup.common.service.autoonboarding;

import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.OpenBankingTokenScope;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.kbciegroup.common.auth.KbcIeGroupAuthMeansBuilder;
import com.yolt.providers.openbanking.ais.kbciegroup.common.model.ContactDto;
import com.yolt.providers.openbanking.ais.kbciegroup.kbcie.KbcIeProperties;
import com.yolt.securityutils.certificate.CertificateParser;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.providerdomain.TokenScope;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.X509Util;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;

import java.security.cert.X509Certificate;
import java.util.*;
import java.util.function.Function;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;
import static com.yolt.providers.openbanking.ais.kbciegroup.common.auth.KbcIeGroupAuthMeansBuilder.*;

@RequiredArgsConstructor
public class KbcIeGroupAutoOnboardingServiceV1 {

    private final HttpClientFactory httpClientFactory;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getDefaultAuthenticationMeansFunction;
    private final JwtCreator jwtCreator;
    private final ProviderIdentification providerIdentification;
    private final SignatureAlgorithm signingAlgorithm;
    private final KbcIeProperties properties;


    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        Map<String, TypedAuthenticationMeans> autoConfiguredMeans = new HashMap<>();
        autoConfiguredMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        autoConfiguredMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        return autoConfiguredMeans;
    }

    public Map<String, BasicAuthenticationMean> register(UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        Map<String, BasicAuthenticationMean> mutableMeans = new HashMap<>(urlAutoOnboardingRequest.getAuthenticationMeans());
        if (!urlAutoOnboardingRequest.getAuthenticationMeans().containsKey(KbcIeGroupAuthMeansBuilder.CLIENT_ID_NAME)) {
            try {
                invokeRegistration(urlAutoOnboardingRequest)
                        .ifPresent(response -> {
                            BasicAuthenticationMean clientIdMean = new BasicAuthenticationMean(CLIENT_ID_STRING.getType(),
                                    response.getClientId());
                            BasicAuthenticationMean clientSecretMean = new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(),
                                    response.getClientSecret());
                            mutableMeans.put(KbcIeGroupAuthMeansBuilder.CLIENT_ID_NAME, clientIdMean);
                            mutableMeans.put(KbcIeGroupAuthMeansBuilder.CLIENT_SECRET_NAME, clientSecretMean);
                        });
            } catch (RestClientException | IllegalStateException | TokenInvalidException e) {
                throw new AutoOnboardingException(providerIdentification.getIdentifier(), "Auto-onboarding failed for KBC IE", e);
            }
        }

        return mutableMeans;
    }

    private Optional<AutoOnboardingResponse> invokeRegistration(UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException {
        Map<String, BasicAuthenticationMean> authMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        DefaultAuthMeans defaultAuthMeans = getDefaultAuthenticationMeansFunction.apply(authMeans);
        HttpClient httpClient = httpClientFactory.createHttpClient(urlAutoOnboardingRequest.getRestTemplateManager(),
                defaultAuthMeans,
                providerIdentification.getDisplayName());

        String jws = prepareSignedJws(defaultAuthMeans.getSigningCertificate(), urlAutoOnboardingRequest.getSigner(), authMeans, urlAutoOnboardingRequest.getRedirectUrls(), urlAutoOnboardingRequest.getScopes());
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/jwt");
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return Optional.of(
                httpClient.exchange(properties.getRegistrationUrl(), HttpMethod.POST, new HttpEntity(jws, headers), ProviderClientEndpoints.REGISTER, AutoOnboardingResponse.class).getBody());

    }

    private String prepareSignedJws(X509Certificate certificate, Signer signer, Map<String, BasicAuthenticationMean> authMeans, List<String> redirectUris, Set<TokenScope> scopes) {
        String thumbprint = X509Util.x5t(certificate);
        OpenBankingTokenScope tokenScope = OpenBankingTokenScope.getByTokenScopes(scopes);
        String organizationId = CertificateParser.getOrganizationIdentifier(certificate);
        String softwareId = authMeans.get(SOFTWARE_ID_NAME).getValue();
        JwtClaims ssaJwtClaims = jwtCreator.createSsaClaims(redirectUris,
                authMeans.get(CLIENT_NAME_NAME).getValue(),
                authMeans.get(CLIENT_DESCRIPTION_NAME).getValue(),
                authMeans.get(JWKS_ENDPOINT_NAME).getValue(),
                organizationId,
                softwareId,
                createContactList(authMeans));
        String signedSsa = signClaims(ssaJwtClaims, signer,
                UUID.fromString(authMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()),
                thumbprint,
                SignatureAlgorithm.SHA256_WITH_RSA);

        JwtClaims registrationJwtClaims = jwtCreator.createRegistrationClaims(organizationId,
                authMeans.get(INSTITUTION_ID_NAME).getValue(),
                softwareId,
                redirectUris,
                tokenScope.getRegistrationScope(),
                signedSsa,
                signingAlgorithm.getJsonSignatureAlgorithm());
        return signClaims(registrationJwtClaims, signer,
                UUID.fromString(authMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()),
                thumbprint,
                SignatureAlgorithm.SHA256_WITH_RSA);
    }

    private List<ContactDto> createContactList(Map<String, BasicAuthenticationMean> authMeans) {
        ContactDto businessContact = new ContactDto(
                authMeans.get(BUSINESS_CONTACT_NAME_NAME).getValue(),
                authMeans.get(BUSINESS_CONTACT_EMAIL_NAME).getValue(),
                authMeans.get(BUSINESS_CONTACT_PHONE_NAME).getValue()
        );
        ContactDto technicalContact = new ContactDto(
                authMeans.get(TECHNICAL_CONTACT_NAME_NAME).getValue(),
                authMeans.get(TECHNICAL_CONTACT_EMAIL_NAME).getValue(),
                authMeans.get(TECHNICAL_CONTACT_PHONE_NAME).getValue()
        );
        return List.of(businessContact, technicalContact);
    }

    private String signClaims(JwtClaims claims, Signer signer, UUID signingKeyId, String thumbprint, SignatureAlgorithm signatureAlgorithm) {
        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(signatureAlgorithm.getJsonSignatureAlgorithm());
        jws.setKeyIdHeaderValue(thumbprint);
        jws.setHeader("typ", "JWT");
        jws.setPayload(claims.toJson());
        return signer.sign(jws, signingKeyId, signatureAlgorithm).getCompactSerialization();
    }
}

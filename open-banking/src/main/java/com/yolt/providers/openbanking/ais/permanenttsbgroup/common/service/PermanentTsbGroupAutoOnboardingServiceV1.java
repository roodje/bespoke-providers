package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.ProviderClientEndpoints;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.ProviderIdentification;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClientFactory;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.autoonboarding.PermanentTsbGroupRegistrationClaims;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.dto.AutoOnboardingResponse;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.oauth2.PermanentTsbGroupTppSignatureCertificateHeaderProducer;
import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.properties.PermanentTsbGroupProperties;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.NumericDate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

import java.security.cert.CertificateEncodingException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;
import static com.yolt.providers.openbanking.ais.permanenttsbgroup.common.auth.PermanentTsbGroupAuthMeansBuilder.*;

@RequiredArgsConstructor
public class PermanentTsbGroupAutoOnboardingServiceV1 {

    private static final String TYP_HEADER = "typ";
    private static final String JWT = "JWT";
    private static final String TPP_SIGNATURE_CERTIFICATE_HEADER_NAME = "tpp-signature-certificate";
    private static final String ALGORITHM = AlgorithmIdentifiers.RSA_PSS_USING_SHA256;
    private static final List<String> AUTO_ON_BOARDING_UNNECESSARY_MEANS = Arrays.asList(CLIENT_ID_NAME, CLIENT_SECRET_NAME);

    private final PermanentTsbGroupProperties properties;
    private final HttpClientFactory httpClientFactory;
    private final ObjectMapper objectMapper;
    private final Function<Map<String, BasicAuthenticationMean>, DefaultAuthMeans> getAuthenticationMeans;
    private final ProviderIdentification providerIdentification;
    private final PermanentTsbGroupTppSignatureCertificateHeaderProducer tppSignatureCertificateHeaderProducer;

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
            } catch (RestClientException | IllegalStateException | TokenInvalidException | CertificateEncodingException e) {
                throw new AutoOnboardingException(providerIdentification.getIdentifier(), "Auto-onboarding failed for Permanent TSB Bank", e);
            }
        }

        return mutableMeans;
    }

    private Optional<AutoOnboardingResponse> invokeRegistration(UrlAutoOnboardingRequest urlAutoOnboardingRequest) throws TokenInvalidException, CertificateEncodingException {
        Map<String, BasicAuthenticationMean> authenticationMeans = urlAutoOnboardingRequest.getAuthenticationMeans();
        DefaultAuthMeans authMeans = getAuthenticationMeans.apply(authenticationMeans);
        String signingKeyId = authenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue();

        JsonWebSignature jws = createJws(authenticationMeans, urlAutoOnboardingRequest);
        String requestPayload = urlAutoOnboardingRequest.getSigner().sign(jws, UUID.fromString(signingKeyId), SignatureAlgorithm.SHA256_WITH_RSA_PSS).getCompactSerialization();
        var responseBody = exchange(urlAutoOnboardingRequest.getRestTemplateManager(), authMeans, requestPayload);
        if (responseBody != null) {
            return Optional.ofNullable(decodeJsonWebToken(responseBody));
        }
        return Optional.empty();
    }

    private String exchange(RestTemplateManager restTemplateManager,
                            DefaultAuthMeans authMeans,
                            String payload) throws TokenInvalidException, CertificateEncodingException {
        HttpClient httpClient = httpClientFactory.createHttpClient(restTemplateManager, authMeans, providerIdentification.getDisplayName());

        HttpHeaders headers = new HttpHeaders();
        headers.set(TPP_SIGNATURE_CERTIFICATE_HEADER_NAME, tppSignatureCertificateHeaderProducer.getTppSignatureCertificateHeaderValue(authMeans.getSigningCertificate()));

        ResponseEntity<String> responseEntity = httpClient.exchange(
                properties.getRegistrationUrl(),
                HttpMethod.POST,
                new HttpEntity<>(payload, headers),
                ProviderClientEndpoints.REGISTER,
                String.class);
        return responseEntity.getBody();
    }

    protected JsonWebSignature createJws(Map<String, BasicAuthenticationMean> authenticationMeans,
                                         UrlAutoOnboardingRequest urlAutoOnboardingRequest) {
        NumericDate issuedAtTime = NumericDate.now();
        issuedAtTime.addSeconds(-3600);
        JwtClaims claims = new JwtClaims();
        claims.setIssuedAt(issuedAtTime);
        claims.setExpirationTimeMinutesInTheFuture(60);
        claims.setJwtId(UUID.randomUUID().toString().toUpperCase());
        claims.setClaim(PermanentTsbGroupRegistrationClaims.ORGANISATION_NAME.getValue(), authenticationMeans.get(ORGANISATION_NAME_NAME).getValue());
        claims.setClaim(PermanentTsbGroupRegistrationClaims.APPLICATION_NAME.getValue(), authenticationMeans.get(APPLICATION_NAME_NAME).getValue());
        claims.setClaim(PermanentTsbGroupRegistrationClaims.REDIRECT_URI.getValue(), String.join(",", urlAutoOnboardingRequest.getRedirectUrls()));
        claims.setClaim(PermanentTsbGroupRegistrationClaims.BUSINESS_CONTACT_NAME.getValue(), authenticationMeans.get(BUSINESS_CONTACT_NAME_NAME).getValue());
        claims.setClaim(PermanentTsbGroupRegistrationClaims.BUSINESS_CONTACT_EMAIL.getValue(), authenticationMeans.get(BUSINESS_CONTACT_EMAIL_NAME).getValue());
        claims.setClaim(PermanentTsbGroupRegistrationClaims.BUSINESS_CONTACT_PHONE.getValue(), authenticationMeans.get(BUSINESS_CONTACT_PHONE_NAME).getValue());
        claims.setClaim(PermanentTsbGroupRegistrationClaims.TECHNICAL_CONTACT_NAME.getValue(), authenticationMeans.get(TECHNICAL_CONTACT_NAME_NAME).getValue());
        claims.setClaim(PermanentTsbGroupRegistrationClaims.TECHNICAL_CONTACT_EMAIL.getValue(), authenticationMeans.get(TECHNICAL_CONTACT_EMAIL_NAME).getValue());
        claims.setClaim(PermanentTsbGroupRegistrationClaims.TECHNICAL_CONTACT_PHONE.getValue(), authenticationMeans.get(TECHNICAL_CONTACT_PHONE_NAME).getValue());

        JsonWebSignature jws = new JsonWebSignature();
        jws.setAlgorithmHeaderValue(ALGORITHM);
        jws.setKeyIdHeaderValue(authenticationMeans.get(SIGNING_KEY_HEADER_ID_NAME).getValue());
        jws.setPayload(claims.toJson());
        jws.setHeader(TYP_HEADER, JWT);
        return jws;
    }

    private AutoOnboardingResponse decodeJsonWebToken(String jsonWebToken) {
        String[] chunks = jsonWebToken.split("\\.");
        String responsePayload = new String(Base64.getDecoder().decode(chunks[1]));
        try {
            return objectMapper.readValue(responsePayload, AutoOnboardingResponse.class);
        } catch (JsonProcessingException e) {
            throw new AutoOnboardingException(providerIdentification.getIdentifier(), "Auto-onboarding failed for Permanent TSB Bank", e);
        }
    }
}
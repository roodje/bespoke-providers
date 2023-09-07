package com.yolt.providers.stet.lclgroup.common.onboarding;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.yolt.providers.common.ais.url.UrlAutoOnboardingRequest;
import com.yolt.providers.stet.generic.domain.SignatureData;
import com.yolt.providers.stet.lclgroup.common.auth.LclGroupClientConfiguration;
import lombok.AllArgsConstructor;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;

import static com.nimbusds.jose.util.X509CertUtils.computeSHA256Thumbprint;
import static com.yolt.providers.common.constants.OAuth.*;
import static com.yolt.providers.stet.lclgroup.common.auth.LclGroupClientConfiguration.CLIENT_ID_NAME;
import static java.util.Collections.singletonList;

@AllArgsConstructor
public class LclGroupAutoOnBoardingService {

    private static final String SCOPE = "aisp,pisp";
    private static final String TLS_CLIENT_AUTH = "tls_client_auth";

    public Optional<LclGroupClientRegistration> register(UrlAutoOnboardingRequest autoOnBoardingRequest,
                                                         LclGroupClientConfiguration clientConfiguration,
                                                         LclRegistrationHttpClient httpClient,
                                                         SignatureData signatureData) {
        if (!autoOnBoardingRequest.getAuthenticationMeans().containsKey(CLIENT_ID_NAME)) {
            return invokeRegistration(autoOnBoardingRequest, httpClient, signatureData, clientConfiguration);
        }
        return Optional.empty();
    }

    public void updateExistingRegistration(String clientId,
                                           LclRegistrationHttpClient httpClient,
                                           SignatureData signatureData,
                                           LclGroupClientConfiguration clientConfiguration) {
        LclRegistrationResponse registrationResponse = httpClient.readRegistration(clientId, signatureData);

        LclRegistrationUpdateRequest registrationRequest =
                prepareRegistrationUpdateRequestFromExistingRegistration(registrationResponse, clientConfiguration);

        httpClient.updateRegistration(clientId, registrationRequest, signatureData);
    }

    private Optional<LclGroupClientRegistration> invokeRegistration(UrlAutoOnboardingRequest autoOnBoardingRequest,
                                                                    LclRegistrationHttpClient httpClient,
                                                                    SignatureData signatureData,
                                                                    LclGroupClientConfiguration clientConfiguration) {
        LclRegistrationRequest registrationRequest = prepareRegistrationRequest(autoOnBoardingRequest, clientConfiguration);
        LclGroupClientRegistration lclGroupClientRegistration = httpClient.createRegistration(registrationRequest, signatureData);

        return Optional.of(lclGroupClientRegistration);
    }

    private LclRegistrationRequest prepareRegistrationRequest(UrlAutoOnboardingRequest autoOnBoardingRequest,
                                                              LclGroupClientConfiguration clientConfiguration) {
        try {
            return LclRegistrationRequest.builder()
                    .clientName(clientConfiguration.getClientName())
                    .redirectUris(singletonList(autoOnBoardingRequest.getBaseClientRedirectUrl() + "/"))
                    .grantTypes(Arrays.asList(
                            AUTHORIZATION_CODE,
                            REFRESH_TOKEN,
                            CLIENT_CREDENTIALS))
                    .responsesTypes(singletonList(CODE))
                    .contacts(singletonList(clientConfiguration.getClientEmail()))
                    .providerLegalId(clientConfiguration.getProviderLegalName())
                    .scope(SCOPE)
                    .tokenEndpointAuthMethod(TLS_CLIENT_AUTH)
                    .jwks(new JsonWebKeySet()
                            .addWebKey(JsonWebKey.builder()
                                    .kty("RSA")
                                    .use("sig")
                                    .keysOps(singletonList("verify"))
                                    .kid(JWK.parse(clientConfiguration.getClientSigningCertificate()).getKeyID())
                                    .x5c(singletonList(encodeCertWithBase64(clientConfiguration.getClientSigningCertificate())))
                                    .x5ts256(computeSHA256Thumbprint(clientConfiguration.getClientSigningCertificate()).toString())
                                    .build()))
                    .build();
        } catch (CertificateEncodingException | JOSEException e) {
            throw new LclGroupAutoOnBoardingException("Couldn't encode or parse certificate for LCL", e);
        }
    }

    private LclRegistrationUpdateRequest prepareRegistrationUpdateRequestFromExistingRegistration(LclRegistrationResponse registrationResponse,
                                                                                                  LclGroupClientConfiguration clientConfiguration) {
        try {
            return LclRegistrationUpdateRequest.builder()
                    .clientName(registrationResponse.getClientName() != null ? registrationResponse.getClientName() : clientConfiguration.getClientName())
                    .redirectUris(registrationResponse.getRedirectUris())
                    .grantTypes(registrationResponse.getGrantTypes())
                    .responsesTypes(registrationResponse.getResponsesTypes())
                    .contacts(registrationResponse.getContacts())
                    .clientId(registrationResponse.getClientId())
                    .providerLegalId(registrationResponse.getProviderLegalId())
                    .scope(registrationResponse.getScope())
                    .tokenEndpointAuthMethod(registrationResponse.getTokenEndpointAuthMethod())
                    .jwks(new JsonWebKeySet()
                            .addWebKey(JsonWebKey.builder()
                                    .kty("RSA")
                                    .use("sig")
                                    .keysOps(singletonList("verify"))
                                    .kid(JWK.parse(clientConfiguration.getClientSigningCertificate()).getKeyID())
                                    .x5c(singletonList(encodeCertWithBase64(clientConfiguration.getClientSigningCertificate())))
                                    .x5ts256(computeSHA256Thumbprint(clientConfiguration.getClientSigningCertificate()).toString())
                                    .build()))
                    .build();
        } catch (CertificateEncodingException | JOSEException e) {
            throw new LclGroupAutoOnBoardingException("Couldn't encode or parse certificate for LCL", e);
        }
    }

    private String encodeCertWithBase64(X509Certificate certificate) throws CertificateEncodingException {
        return Base64.getEncoder().encodeToString(certificate.getEncoded());
    }
}

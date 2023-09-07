package com.yolt.providers.stet.cicgroup.common.mapper.registration;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.stet.cicgroup.common.dto.CicGroupCertificateJsonWebKey;
import com.yolt.providers.stet.cicgroup.common.dto.CicGroupJsonWebKeySet;
import com.yolt.providers.stet.cicgroup.common.dto.CicGroupRegistrationRequestDTO;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.dto.registration.StetTokenEndpointAuthMethod;
import com.yolt.providers.stet.generic.mapper.registration.RegistrationRequestMapper;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class CicGroupRegistrationRequestMapper implements RegistrationRequestMapper {

    private static final String KTY_VALUE = "RSA";

    @Override
    public Object mapToRegistrationRequest(RegistrationRequest registrationRequest) {
        DefaultAuthenticationMeans authMeans = registrationRequest.getAuthMeans();;
        return CicGroupRegistrationRequestDTO.builder()
                .redirectUris(List.of(registrationRequest.getRedirectUrl()))
                .tokenEndpointAuthMethod(StetTokenEndpointAuthMethod.TLS_CLIENT_AUTH.getValue())
                .grantTypes(List.of(OAuth.AUTHORIZATION_CODE, OAuth.REFRESH_TOKEN))
                .responseTypes(List.of(OAuth.CODE))
                .clientName(authMeans.getClientName())
                .scope(Scope.AISP_EXTENDED_TRANSACTION_HISTORY.getValue())
                .contacts(List.of(authMeans.getClientEmail()))
                .jwks(new CicGroupJsonWebKeySet(List.of(CicGroupCertificateJsonWebKey.builder()
                .kty(KTY_VALUE)
                .x5c(List.of(createBase64SigningCertificate(authMeans.getClientSigningCertificate(), registrationRequest.getProviderIdentifier()))).build())))
                .build();
    }

    public Object mapToUpdateRegistrationRequest(RegistrationRequest registrationRequest, CicGroupRegistrationRequestDTO currentRegistration) {
        DefaultAuthenticationMeans authMeans = registrationRequest.getAuthMeans();
            List<String> redirectUris = currentRegistration.getRedirectUris();
        CicGroupJsonWebKeySet updatedJsonWebKeySet = currentRegistration.getJwks();

        boolean isRedirectUrlRegistered = isUrlRegistered(registrationRequest, currentRegistration.getRedirectUris());
        boolean isCertificateRegistered = isCertificateRegistered(currentRegistration, authMeans, registrationRequest.getProviderIdentifier());

        if(!isRedirectUrlRegistered) {
            redirectUris.add(registrationRequest.getRedirectUrl());
        }

        if (!isCertificateRegistered) {
            updatedJsonWebKeySet.getKeys().add(CicGroupCertificateJsonWebKey.builder()
                    .kty(KTY_VALUE)
                    .x5c(List.of(createBase64SigningCertificate(authMeans.getClientSigningCertificate(), registrationRequest.getProviderIdentifier())))
                    .build());
        }

        if(isRedirectUrlRegistered && isCertificateRegistered) {
            throw new AutoOnboardingException(registrationRequest.getProviderIdentifier(),
                    "No change in redirect url or signing certificate, request of updating registration is declined",
                    new IllegalArgumentException());
        }

        return CicGroupRegistrationRequestDTO.builder()
                .redirectUris(new ArrayList<>(redirectUris))
                .tokenEndpointAuthMethod(StetTokenEndpointAuthMethod.TLS_CLIENT_AUTH.getValue())
                .grantTypes(List.of(OAuth.AUTHORIZATION_CODE, OAuth.REFRESH_TOKEN))
                .responseTypes(List.of(OAuth.CODE))
                .clientName(currentRegistration.getClientName())
                .scope(Scope.AISP_EXTENDED_TRANSACTION_HISTORY.getValue())
                .contacts(List.of(authMeans.getClientEmail()))
                .jwks(updatedJsonWebKeySet)
                .build();
    }

    private boolean isUrlRegistered(RegistrationRequest registrationRequest, List<String> redirectUris) {
        return redirectUris.contains(registrationRequest.getRedirectUrl());
    }

    private boolean isCertificateRegistered(CicGroupRegistrationRequestDTO currentRegistration, DefaultAuthenticationMeans authMeans, String providerIdentifier){
       return  currentRegistration.getJwks()
                .getKeys()
                .stream()
                .reduce(
                        false,
                        (contains, key) -> contains = contains || key.getX5c().contains(createBase64SigningCertificate(authMeans.getClientSigningCertificate(), providerIdentifier)),
                        (firstResult, secondResult) -> firstResult || secondResult
                );
    }

    private String createBase64SigningCertificate(X509Certificate signingCertificate, String providerIdentifier) {
        String result;
        try {
            result = Base64.getEncoder().encodeToString(signingCertificate.getEncoded());
        } catch (CertificateEncodingException e) {
            throw new AutoOnboardingException(providerIdentifier, "Unable to encode signing certificate", e);
        }
        return result;
    }
}

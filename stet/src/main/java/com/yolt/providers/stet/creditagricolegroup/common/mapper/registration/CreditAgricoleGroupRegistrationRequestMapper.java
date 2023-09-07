package com.yolt.providers.stet.creditagricolegroup.common.mapper.registration;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.common.exception.AutoOnboardingException;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.dto.registration.StetKeyDTO;
import com.yolt.providers.stet.generic.dto.registration.StetRegistrationRequestDTO;
import com.yolt.providers.stet.generic.mapper.registration.RegistrationRequestMapper;
import com.yolt.securityutils.certificate.CertificateParser;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

public class CreditAgricoleGroupRegistrationRequestMapper implements RegistrationRequestMapper {

    @Override
    public Object mapToRegistrationRequest(RegistrationRequest request) {
        List<String> grantTypes = Arrays.asList(OAuth.CLIENT_CREDENTIALS, OAuth.AUTHORIZATION_CODE, OAuth.REFRESH_TOKEN);
        grantTypes.replaceAll(String::toUpperCase);

        return prepareRegistrationRequestDTO(request)
                .grantTypes(grantTypes)
                .build();
    }

    @Override
    public Object mapToUpdateRegistrationRequest(RegistrationRequest request) {
        return prepareRegistrationRequestDTO(request)
                .registrationAccessToken("NOTAPPLICABLE")
                .build();
    }

    private StetRegistrationRequestDTO.StetRegistrationRequestDTOBuilder prepareRegistrationRequestDTO(RegistrationRequest request) {
        DefaultAuthenticationMeans authMeans = request.getAuthMeans();
        X509Certificate signingCertificate = authMeans.getClientSigningCertificate();

        try {
            StetKeyDTO jwks = StetKeyDTO.builder()
                    .kty("RSA")
                    .kid(authMeans.getClientSigningKeyId().toString())
                    .x5c(Base64.getEncoder().encodeToString(signingCertificate.getEncoded()))
                    .build();

            return StetRegistrationRequestDTO.builder()
                    .clientName(authMeans.getClientName())
                    .contacts(Collections.singletonList(authMeans.getClientEmail()))
                    .providerLegalId(CertificateParser.getOrganizationIdentifier(signingCertificate))
                    .redirectUris(Collections.singletonList(request.getRedirectUrl()))
                    .jwks(Collections.singletonList(jwks));

        } catch(CertificateEncodingException e) {
            throw new AutoOnboardingException(request.getProviderIdentifier(), "Unable to encode signing certificate", e);
        }
    }
}

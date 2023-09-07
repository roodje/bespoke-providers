package com.yolt.providers.stet.labanquepostalegroup.common.mapper.registration;

import com.yolt.providers.common.constants.OAuth;
import com.yolt.providers.stet.generic.dto.registration.*;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.Scope;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.providers.stet.generic.mapper.registration.RegistrationRequestMapper;
import com.yolt.providers.stet.labanquepostalegroup.labanquepostale.auth.LaBanquePostaleAuthenticationMeans;
import com.yolt.securityutils.certificate.CertificateParser;

import java.util.Collections;

public class LaBanquePostaleGroupRegistrationRequestMapper implements RegistrationRequestMapper {

    @Override
    public Object mapToRegistrationRequest(RegistrationRequest request) {
        LaBanquePostaleAuthenticationMeans authMeans = (LaBanquePostaleAuthenticationMeans) request.getAuthMeans();

        String scope = Scope.AISP_PISP.getValue();
        String clientName = authMeans.getClientName();

        return StetRegistrationRequestDTO.builder()
                .contacts(Collections.singletonList(authMeans.getClientEmail()))
                .clientName(clientName)
                .providerLegalId(CertificateParser.getOrganizationIdentifier(authMeans.getClientTransportCertificateChain()[0]))
                .scope(scope)
                .redirectUris(Collections.singletonList(request.getRedirectUrl()))
                .grantTypes(Collections.singletonList(OAuth.AUTHORIZATION_CODE))
                .tokenEndpointAuthMethod(StetTokenEndpointAuthMethod.TLS_CLIENT_AUTH.getValue())
                .description(String.format("Application for %s client with %s scope(s)", clientName, scope))
                .build();
    }
}

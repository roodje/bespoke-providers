package com.yolt.providers.stet.bpcegroup.common.onboarding;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.yolt.providers.stet.bpcegroup.common.auth.BpceGroupAuthenticationMeans;
import com.yolt.providers.stet.generic.mapper.registration.RegistrationRequestMapper;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import com.yolt.securityutils.certificate.CertificateParser;
import lombok.SneakyThrows;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;

import static com.nimbusds.jose.util.X509CertUtils.computeSHA256Thumbprint;
import static com.yolt.providers.common.constants.OAuth.*;
import static java.util.Collections.singletonList;

public class BpceGroupRegistrationRequestMapper implements RegistrationRequestMapper {

    private static final String TLS_CLIENT_AUTH = "tls_client_auth";
    private static final String SCOPE = "aisp,pisp";

    @SneakyThrows
    @Override
    public Object mapToRegistrationRequest(RegistrationRequest registrationRequest) {
        if (!(registrationRequest.getAuthMeans() instanceof BpceGroupAuthenticationMeans)) {
            throw new IllegalStateException("Bpce Group Registration Request Mapper requires BpceGroupAuthenticationMeans");
        }
        var authMeans = (BpceGroupAuthenticationMeans) registrationRequest.getAuthMeans();
        return mapToRegistrationRequestWithBpceAuthMeans(registrationRequest, authMeans);
    }

    @Override
    public Object mapToUpdateRegistrationRequest(RegistrationRequest registrationRequest) {
        return mapToRegistrationRequest(registrationRequest);
    }

    private String encodeCertWithBase64(X509Certificate certificate) throws CertificateEncodingException {
        return Base64.getEncoder().encodeToString(certificate.getEncoded());
    }

    private BpceRegistrationRequestDTO mapToRegistrationRequestWithBpceAuthMeans(RegistrationRequest registrationRequest, BpceGroupAuthenticationMeans authMeans) throws JOSEException, CertificateEncodingException {
        var contact = new BpceRegistrationRequestDTO.Contact();
        contact.setContactName(authMeans.getClientName());
        contact.setEmail(authMeans.getClientEmail());
        contact.setPhoneNumber(authMeans.getContactPhone());
        var bpceBuilder = BpceRegistrationRequestDTO.builder()
                .clientName(authMeans.getClientName())
                .redirectUris(singletonList(registrationRequest.getRedirectUrl()))
                .grantTypes(Arrays.asList(
                        AUTHORIZATION_CODE,
                        REFRESH_TOKEN,
                        CLIENT_CREDENTIALS))
                .responsesTypes(singletonList(CODE))
                .contact(contact)
                .providerLegalId(CertificateParser.getOrganizationIdentifier(authMeans.getClientTransportCertificate()))
                .scope(SCOPE)
                .tokenEndpointAuthMethod(TLS_CLIENT_AUTH)
                .jwks(new BpceRegistrationRequestDTO.JsonWebKeySet()
                        .addWebKey(BpceRegistrationRequestDTO.JsonWebKeySet.JsonWebKey.builder()
                                .kty("RSA")
                                .use("sig")
                                .alg("RS256")
                                .keysOps(singletonList("verify"))
                                .kid(JWK.parse(authMeans.getClientSigningCertificate()).getKeyID())
                                .x5c(singletonList(encodeCertWithBase64(authMeans.getClientSigningCertificate())))
                                .x5ts256(computeSHA256Thumbprint(authMeans.getClientSigningCertificate()).toString())
                                .build()))
                .softwareId(authMeans.getClientName());
        if (authMeans.getClientId() != null) {
            bpceBuilder.clientId(authMeans.getClientId());
        }
        return bpceBuilder.build();
    }
}

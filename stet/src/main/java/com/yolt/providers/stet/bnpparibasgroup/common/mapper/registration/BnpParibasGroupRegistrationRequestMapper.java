package com.yolt.providers.stet.bnpparibasgroup.common.mapper.registration;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.RSAKey;
import com.yolt.providers.stet.bnpparibasgroup.common.dto.BnpParibasGroupRegistrationRequestDTO;
import com.yolt.providers.stet.bnpparibasgroup.common.dto.BnpParibasGroupRegistrationRequestUpdateDTO;
import com.yolt.providers.stet.bnpparibasgroup.common.exception.BnpParibasGroupOnboardingException;
import com.yolt.providers.stet.bnpparibasgroup.common.model.JsonWebKeySet;
import com.yolt.providers.stet.bnpparibasgroup.common.onboarding.JsonWebKey;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.mapper.registration.RegistrationRequestMapper;
import com.yolt.providers.stet.generic.service.registration.request.RegistrationRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.UUID;

import static com.yolt.providers.stet.bnpparibasgroup.common.dto.ContextEnum.PSD2;
import static com.yolt.providers.stet.bnpparibasgroup.common.dto.GrantTypesEnum.*;
import static com.yolt.providers.stet.bnpparibasgroup.common.dto.ResponseTypesEnum.CODE;
import static com.yolt.providers.stet.bnpparibasgroup.common.dto.TokenEndpointAuthMethodEnum.TLS_CLIENT_AUTH;
import static com.yolt.securityutils.certificate.CertificateParser.getOrganization;
import static com.yolt.securityutils.certificate.CertificateParser.getOrganizationIdentifier;
import static com.yolt.securityutils.signing.SignatureAlgorithm.SHA256_WITH_RSA;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class BnpParibasGroupRegistrationRequestMapper implements RegistrationRequestMapper {

    @Override
    public Object mapToRegistrationRequest(RegistrationRequest registrationRequest) {
        DefaultAuthenticationMeans authMeans = registrationRequest.getAuthMeans();
        X509Certificate qsealCertificate = authMeans.getClientSigningCertificate();
        if (StringUtils.isNotBlank(registrationRequest.getAuthMeans().getClientId())) {
            return new BnpParibasGroupRegistrationRequestUpdateDTO()
                    .clientId(authMeans.getClientId())
                    .tokenEndpointAuthMethod(TLS_CLIENT_AUTH)
                    .grantTypes(asList(AUTHORIZATION_CODE, REFRESH_TOKEN, CLIENT_CREDENTIALS))
                    .responseTypes(singletonList(CODE))
                    .clientName(getOrganization(qsealCertificate))
                    .clientUri(authMeans.getClientWebsiteUri())
                    .logoUri(authMeans.getClientLogoUri())
                    .scope(String.join(" ", getScopes()))
                    .redirectUris(List.of(registrationRequest.getRedirectUrl()))
                    .contacts(singletonList(authMeans.getClientEmail()))
                    .providerLegalId(getOrganizationIdentifier(qsealCertificate))
                    .jwks(getSigningCertificateAsKeySet(qsealCertificate, authMeans.getClientSigningKeyId()))
                    .context(PSD2);
        }
        return new BnpParibasGroupRegistrationRequestDTO()
                .tokenEndpointAuthMethod(TLS_CLIENT_AUTH)
                .grantTypes(asList(AUTHORIZATION_CODE, REFRESH_TOKEN, CLIENT_CREDENTIALS))
                .responseTypes(singletonList(CODE))
                .clientName(getOrganization(qsealCertificate))
                .clientUri(authMeans.getClientWebsiteUri())
                .logoUri(authMeans.getClientLogoUri())
                .scope(String.join(" ", getScopes()))
                .redirectUris(List.of(registrationRequest.getRedirectUrl()))
                .contacts(singletonList(authMeans.getClientEmail()))
                .providerLegalId(getOrganizationIdentifier(qsealCertificate))
                .jwks(getSigningCertificateAsKeySet(qsealCertificate, authMeans.getClientSigningKeyId()))
                .context(PSD2);
    }

    private String[] getScopes() {
        return new String[]{
                "aisp",
                "pisp"
        };
    }

    private JsonWebKeySet getSigningCertificateAsKeySet(X509Certificate qsealCertificate,
                                                        UUID signingKid) {
        try {
            RSAKey publicKey = RSAKey.parse(qsealCertificate);

            JsonWebKeySet jsonWebKeySet = new JsonWebKeySet();
            jsonWebKeySet.addWebKey(JsonWebKey.builder()
                    .kid(signingKid.toString())
                    .kty(SHA256_WITH_RSA.getJsonSignatureAlgorithm())
                    .use(publicKey.getKeyUse().getValue())
                    .x5c(singletonList(Base64Utils.encodeToString(qsealCertificate.getEncoded())))
                    .n(publicKey.getModulus().toString())
                    .build());

            return jsonWebKeySet;
        } catch (JOSEException e) {
            throw new BnpParibasGroupOnboardingException("Failed to parse QSEAL certificate to public RSA key while processing in auto onboarding flow", e);
        } catch (CertificateEncodingException e) {
            throw new BnpParibasGroupOnboardingException("Failed to encode QSEAL certificate while processing in auto onboarding flow", e);
        }
    }
}




package com.yolt.providers.stet.lclgroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.http.signer.signature.Fingerprint;
import lombok.Builder;
import lombok.Getter;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@Getter
public class LclGroupClientConfiguration extends DefaultAuthenticationMeans {

    private static final String PEM_FORMAT_EXTENSION = ".pem";

    // auto onboarding AuthMeans
    public static final String CLIENT_ID_NAME = "client-id";

    // yap and hsm AuthMeans
    public static final String CLIENT_CONTACT_EMAIL_NAME = "client-contact-email";
    public static final String CLIENT_NAME_NAME = "client-name";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "private-client-transport-key-id";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    public static final String CLIENT_SIGNING_CERTIFICATE_NAME = "client-signing-certificate";
    public static final String CLIENT_SIGNING_KEY_ID_NAME = "private-client-signing-key-id";
    public static final String PROVIDER_LEGAL_ID_NAME = "provider-legal-id";
    private String providerLegalName;

    @Builder(builderMethodName = "extendedBuilder")
    public LclGroupClientConfiguration(
            final String clientId,
            final String clientSecret,
            final X509Certificate clientTransportCertificate,
            final UUID clientTransportKeyId,
            final X509Certificate clientSigningCertificate,
            final UUID clientSigningKeyId,
            final String signingKeyIdHeader,
            final String clientName,
            final String clientEmail,
            final String clientWebsiteUri,
            final String providerLegalName) {
        super(clientId, clientSecret, clientTransportCertificate, clientTransportKeyId, clientSigningCertificate, clientSigningKeyId, signingKeyIdHeader, clientName, clientEmail, clientWebsiteUri);
        this.providerLegalName = providerLegalName;
    }

    public static LclGroupClientConfiguration fromAuthenticationMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        return fromAuthenticationMeansNoClientId(interpreter)
                .clientId(interpreter.getNullableValue(CLIENT_ID_NAME))
                .build();
    }

    public static LclGroupClientConfiguration fromAuthenticationMeansForAutoOnBoarding(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        return fromAuthenticationMeansNoClientId(interpreter)
                .clientName(interpreter.getValue(CLIENT_NAME_NAME))
                .build();
    }

    private static LclGroupClientConfigurationBuilder fromAuthenticationMeansNoClientId(AuthenticationMeansInterpreter interpreter) {
        return LclGroupClientConfiguration.extendedBuilder()
                .clientEmail(interpreter.getValue(CLIENT_CONTACT_EMAIL_NAME))
                .providerLegalName(interpreter.getValue(PROVIDER_LEGAL_ID_NAME))
                .clientSigningKeyId(interpreter.getUUID(CLIENT_SIGNING_KEY_ID_NAME))
                .clientTransportKeyId(interpreter.getUUID(CLIENT_TRANSPORT_KEY_ID_NAME))
                .clientSigningCertificate(interpreter.getCertificate(CLIENT_SIGNING_CERTIFICATE_NAME))
                .clientTransportCertificate(interpreter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE_NAME));
    }

    public String getCertificateUrl(final String s3BaseUrl, final String providerKey) {
        try {
            final String fingerprint = new Fingerprint(getClientSigningCertificate().getEncoded()).toString();
            return s3BaseUrl + "/" + fingerprint + PEM_FORMAT_EXTENSION;
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey,
                    CLIENT_SIGNING_CERTIFICATE_NAME, "Failed to create fingerprint from certificate");
        }
    }
}

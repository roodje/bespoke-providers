package com.yolt.providers.openbanking.ais.kbciegroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;

public class KbcIeGroupAuthMeansBuilder {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String INSTITUTION_ID_NAME = "institution-id";
    public static final String SOFTWARE_ID_NAME = "software-id";
    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME = "signing-private-key-id";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_PRIVATE_KEY_ID_NAME = "transport-private-key-id";
    public static final String JWKS_ENDPOINT_NAME = "jwks-endpoint";
    public static final String CLIENT_NAME_NAME = "client-name";
    public static final String CLIENT_DESCRIPTION_NAME = "client-description";
    public static final String BUSINESS_CONTACT_NAME_NAME = "business-contact-name";
    public static final String BUSINESS_CONTACT_EMAIL_NAME = "business-contact-email";
    public static final String BUSINESS_CONTACT_PHONE_NAME = "business-contact-phone";
    public static final String TECHNICAL_CONTACT_NAME_NAME = "technical-contact-name";
    public static final String TECHNICAL_CONTACT_EMAIL_NAME = "technical-contact-email";
    public static final String TECHNICAL_CONTACT_PHONE_NAME = "technical-contact-phone";

    public static DefaultAuthMeans createAuthenticationMeansForAis(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                   final String providerKey) {
        return prepareDefaultAuthMeansBuilder(typedAuthenticationMeans, providerKey)
                .build();
    }

    protected static DefaultAuthMeans.DefaultAuthMeansBuilder prepareDefaultAuthMeansBuilder(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                                             final String providerKey) {
        return DefaultAuthMeans.builder()
                .institutionId(typedAuthenticationMeans.get(INSTITUTION_ID_NAME).getValue())
                .softwareId(typedAuthenticationMeans.get(SOFTWARE_ID_NAME).getValue())
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME) != null ? typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue() : null)
                .clientSecret(typedAuthenticationMeans.get(CLIENT_SECRET_NAME) != null ? typedAuthenticationMeans.get(CLIENT_SECRET_NAME).getValue() : null)
                .signingCertificate(createCertificate(typedAuthenticationMeans, SIGNING_CERTIFICATE_NAME, providerKey))
                .signingPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(SIGNING_PRIVATE_KEY_ID_NAME).getValue()))
                .transportCertificate(createCertificate(typedAuthenticationMeans, TRANSPORT_CERTIFICATE_NAME, providerKey))
                .transportPrivateKeyId(UUID.fromString(typedAuthenticationMeans.get(TRANSPORT_PRIVATE_KEY_ID_NAME).getValue()));
    }

    public static Supplier<Map<String, TypedAuthenticationMeans>> getTypedAuthenticationMeansSupplier() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(INSTITUTION_ID_NAME, TypedAuthenticationMeans.INSTITUTION_ID_STRING);
        typedAuthenticationMeans.put(SOFTWARE_ID_NAME, TypedAuthenticationMeans.SOFTWARE_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(JWKS_ENDPOINT_NAME, getCustomizedTypedAuthenticationMean("URL of public JWKs endpoint"));
        typedAuthenticationMeans.put(CLIENT_NAME_NAME, getCustomizedTypedAuthenticationMean("Client name"));
        typedAuthenticationMeans.put(CLIENT_DESCRIPTION_NAME, getCustomizedTypedAuthenticationMean("Client description"));
        typedAuthenticationMeans.put(BUSINESS_CONTACT_NAME_NAME, getCustomizedTypedAuthenticationMean("Business contact name"));
        typedAuthenticationMeans.put(BUSINESS_CONTACT_EMAIL_NAME, getCustomizedTypedAuthenticationMean("Business contact e-mail"));
        typedAuthenticationMeans.put(BUSINESS_CONTACT_PHONE_NAME, getCustomizedTypedAuthenticationMean("Business contact phone number"));
        typedAuthenticationMeans.put(TECHNICAL_CONTACT_NAME_NAME, getCustomizedTypedAuthenticationMean("Technical contact name"));
        typedAuthenticationMeans.put(TECHNICAL_CONTACT_EMAIL_NAME, getCustomizedTypedAuthenticationMean("Technical contact e-mail"));
        typedAuthenticationMeans.put(TECHNICAL_CONTACT_PHONE_NAME, getCustomizedTypedAuthenticationMean("Technical contact phone number"));
        return () -> typedAuthenticationMeans;
    }

    private static X509Certificate createCertificate(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                     final String certificateName,
                                                     final String providerKey) {
        try {
            String certificate = typedAuthenticationMeans.get(certificateName).getValue();
            return KeyUtil.createCertificateFromPemFormat(certificate);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, certificateName, "Cannot process certificate from PEM format");
        }
    }

    private static TypedAuthenticationMeans getCustomizedTypedAuthenticationMean(String displayName) {
        return new TypedAuthenticationMeans(displayName, StringType.getInstance(), ONE_LINE_STRING);
    }
}

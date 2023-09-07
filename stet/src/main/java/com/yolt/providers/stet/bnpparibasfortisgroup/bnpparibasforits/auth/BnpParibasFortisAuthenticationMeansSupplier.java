package com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.AuthenticationMeanType;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.config.BnpParibasFortisProperties;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import com.yolt.providers.stet.generic.http.signer.signature.Fingerprint;
import lombok.RequiredArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;

@RequiredArgsConstructor
public class BnpParibasFortisAuthenticationMeansSupplier implements ExtendedAuthenticationMeansSupplier {

    public static final TypedAuthenticationMeans CLIENT_NAME_TYPE = createTypedMean("Client name", StringType.getInstance());
    public static final TypedAuthenticationMeans CLIENT_DESCRIPTION_TYPE = createTypedMean("Client description", StringType.getInstance());
    public static final TypedAuthenticationMeans CLIENT_WEBSITE_URI_TYPE = createTypedMean("Client website uri (URL encoded)", NoWhiteCharacterStringType.getInstance());
    public static final TypedAuthenticationMeans CONTACT_FIRST_NAME_TYPE = createTypedMean("Client contact first name", StringType.getInstance());
    public static final TypedAuthenticationMeans CONTACT_LAST_NAME_TYPE = createTypedMean("Client contact last name", StringType.getInstance());
    public static final TypedAuthenticationMeans CONTACT_EMAIL_TYPE = createTypedMean("Client contact email address", NoWhiteCharacterStringType.getInstance());
    public static final TypedAuthenticationMeans CONTACT_PHONE_TYPE = createTypedMean("Client contact phone number", NoWhiteCharacterStringType.getInstance());

    public static final String CLIENT_NAME = "client-name";
    public static final String CLIENT_DESCRIPTION = "client-description";
    public static final String CLIENT_WEBSITE_URI = "client-website-uri";
    public static final String CONTACT_FIRST_NAME = "contact-first-name";
    public static final String CONTACT_LAST_NAME = "contact-last-name";
    public static final String CONTACT_EMAIL = "contact-email";
    public static final String CONTACT_PHONE = "contact-phone";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";
    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String SIGNING_KEY_ID_NAME = "signing-key-id";
    public static final String CLIENT_ID_STRING_NAME = "client-id";
    public static final String CLIENT_SECRET_STRING_NAME = "client-secret";

    private static final String PEM_FORMAT_EXTENSION = ".pem";

    private final KeyRequirementsProducer keyRequirementsProducer;
    private final BnpParibasFortisProperties properties;

    private static TypedAuthenticationMeans createTypedMean(String displayName, AuthenticationMeanType type) {
        return new TypedAuthenticationMeans(displayName, type, ONE_LINE_STRING);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(CLIENT_ID_STRING_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthMeans.put(CLIENT_SECRET_STRING_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        return typedAuthMeans;
    }

    @Override
    public Map<String, String> getRegisteredAuthMeans(ObjectNode registrationResponse) {
        Map<String, String> registeredAuthMeans = new HashMap<>();
        registeredAuthMeans.put(CLIENT_ID_STRING_NAME, registrationResponse.get("clientId").textValue());
        registeredAuthMeans.put(CLIENT_SECRET_STRING_NAME, registrationResponse.get("clientSecret").textValue());
        return registeredAuthMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(CLIENT_NAME, CLIENT_NAME_TYPE);
        typedAuthMeans.put(CLIENT_DESCRIPTION, CLIENT_DESCRIPTION_TYPE);
        typedAuthMeans.put(CLIENT_WEBSITE_URI, CLIENT_WEBSITE_URI_TYPE);
        typedAuthMeans.put(CONTACT_FIRST_NAME, CONTACT_FIRST_NAME_TYPE);
        typedAuthMeans.put(CONTACT_LAST_NAME, CONTACT_LAST_NAME_TYPE);
        typedAuthMeans.put(CONTACT_EMAIL, CONTACT_EMAIL_TYPE);
        typedAuthMeans.put(CONTACT_PHONE, CONTACT_PHONE_TYPE);
        typedAuthMeans.put(TRANSPORT_CERTIFICATE_NAME, CERTIFICATE_PEM);
        typedAuthMeans.put(TRANSPORT_KEY_ID_NAME, KEY_ID);
        typedAuthMeans.put(SIGNING_CERTIFICATE_NAME, CERTIFICATE_PEM);
        typedAuthMeans.put(SIGNING_KEY_ID_NAME, KEY_ID);
        typedAuthMeans.put(CLIENT_SECRET_STRING_NAME, CLIENT_SECRET_STRING);
        typedAuthMeans.put(CLIENT_ID_STRING_NAME, CLIENT_ID_STRING);
        return typedAuthMeans;
    }

    @Override
    public DefaultAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        X509Certificate signingCertificate = interpreter.getCertificate(SIGNING_CERTIFICATE_NAME);
        return BnpParibasFortisAuthenticationMeans.extendedBuilder()
                .clientName(interpreter.getValue(CLIENT_NAME))
                .clientWebsiteUri(interpreter.getValue(CLIENT_WEBSITE_URI))
                .clientEmail(interpreter.getValue(CONTACT_EMAIL))
                .clientTransportCertificate(interpreter.getCertificate(TRANSPORT_CERTIFICATE_NAME))
                .clientTransportKeyId(interpreter.getUUID(TRANSPORT_KEY_ID_NAME))
                .clientSigningCertificate(signingCertificate)
                .clientSigningKeyId(interpreter.getUUID(SIGNING_KEY_ID_NAME))
                .clientId(interpreter.getNullableValue(CLIENT_ID_STRING_NAME))
                .clientSecret(interpreter.getNullableValue(CLIENT_SECRET_STRING_NAME))
                .clientDescription(interpreter.getValue(CLIENT_DESCRIPTION))
                .contactFirstName(interpreter.getValue(CONTACT_FIRST_NAME))
                .contactLastName(interpreter.getValue(CONTACT_LAST_NAME))
                .contactPhone(interpreter.getValue(CONTACT_PHONE))
                .signingKeyIdHeader(getCertificateUrl(signingCertificate, properties.getS3baseUrl(), providerIdentifier))
                .build();
    }

    private String getCertificateUrl(X509Certificate certificate, String s3BaseUrl, String providerKey) {
        try {
            final String fingerprint = new Fingerprint(certificate.getEncoded()).toString();
            return s3BaseUrl + "/" + fingerprint + PEM_FORMAT_EXTENSION;
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerKey, SIGNING_CERTIFICATE_NAME, "Failed to create fingerprint from certificate");
        }
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(SIGNING_KEY_ID_NAME, SIGNING_CERTIFICATE_NAME));
    }
}

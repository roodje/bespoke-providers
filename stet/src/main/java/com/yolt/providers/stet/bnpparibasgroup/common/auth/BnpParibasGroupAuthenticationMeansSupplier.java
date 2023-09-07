package com.yolt.providers.stet.bnpparibasgroup.common.auth;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.stet.generic.auth.AuthenticationMeansInterpreter;
import com.yolt.providers.stet.generic.auth.ExtendedAuthenticationMeansSupplier;
import com.yolt.providers.stet.generic.auth.keyrequirements.KeyRequirementsProducer;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.http.signer.signature.Fingerprint;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.PASSWORD;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.auth.BnpParibasFortisAuthenticationMeansSupplier.CLIENT_ID_STRING_NAME;
import static com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.auth.BnpParibasFortisAuthenticationMeansSupplier.CLIENT_SECRET_STRING_NAME;

@RequiredArgsConstructor
public class BnpParibasGroupAuthenticationMeansSupplier implements ExtendedAuthenticationMeansSupplier {

    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_CONTACT_EMAIL = "client-contact-email";
    public static final String CLIENT_WEBSITE_URI = "client-website-uri";
    public static final String CLIENT_LOGO_URI = "client-logo-uri";
    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_TRANSPORT_CERTIFICATE = "client-transport-certificate";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME_V2 = "client-transport-certificate-v2";
    public static final String CLIENT_SIGNING_CERTIFICATE_NAME_V2 = "client-signing-certificate-v2";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME_V2 = "client-transport-private-keyid-v2";
    public static final String CLIENT_SIGNING_KEY_ID_NAME_V2 = "client-signing-private-keyid-v2";
    public static final String CLIENT_REGISTRATION_ACCESS_TOKEN_NAME = "client-registration-access-token";
    public static final TypedAuthenticationMeans CLIENT_REGISTRATION_ACCESS_TOKEN_TYPE = new TypedAuthenticationMeans(CLIENT_REGISTRATION_ACCESS_TOKEN_NAME, StringType.getInstance(), PASSWORD);
    private static final String PEM_FORMAT_EXTENSION = ".pem";

    private final DefaultProperties properties;
    private final KeyRequirementsProducer keyRequirementsProducer;

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthMeans() {

        NoWhiteCharacterStringType noWhiteCharacterStringType = NoWhiteCharacterStringType.getInstance();

        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(CLIENT_ID, CLIENT_ID_STRING);
        typedAuthMeans.put(CLIENT_SECRET_NAME, CLIENT_SECRET_STRING);
        typedAuthMeans.put(CLIENT_CONTACT_EMAIL, CLIENT_EMAIL);
        typedAuthMeans.put(CLIENT_WEBSITE_URI, new TypedAuthenticationMeans(CLIENT_WEBSITE_URI, noWhiteCharacterStringType, ONE_LINE_STRING));
        typedAuthMeans.put(CLIENT_LOGO_URI, new TypedAuthenticationMeans(CLIENT_LOGO_URI, noWhiteCharacterStringType, ONE_LINE_STRING));
        typedAuthMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME_V2, CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthMeans.put(CLIENT_SIGNING_CERTIFICATE_NAME_V2, CLIENT_SIGNING_CERTIFICATE_PEM);
        typedAuthMeans.put(CLIENT_SIGNING_KEY_ID_NAME_V2, KEY_ID);
        typedAuthMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME_V2, KEY_ID);
        typedAuthMeans.put(CLIENT_REGISTRATION_ACCESS_TOKEN_NAME, BnpParibasGroupAuthenticationMeansSupplier.CLIENT_REGISTRATION_ACCESS_TOKEN_TYPE);
        return typedAuthMeans;
    }

    @Override
    @SneakyThrows
    public BnpParibasAuthenticationMeans getAuthMeans(Map<String, BasicAuthenticationMean> basicAuthMeans, String providerIdentifier) {
        AuthenticationMeansInterpreter interpreter = new AuthenticationMeansInterpreter(basicAuthMeans, providerIdentifier);
        X509Certificate signingCertificate = interpreter.getCertificate(CLIENT_SIGNING_CERTIFICATE_NAME_V2);
        return BnpParibasAuthenticationMeans.extendedBuilder()
                .clientId(interpreter.getNullableValue(CLIENT_ID))
                .clientSecret(interpreter.getNullableValue(CLIENT_SECRET_NAME))
                .registrationAccessToken(interpreter.getNullableValue(CLIENT_REGISTRATION_ACCESS_TOKEN_NAME))
                .clientSigningCertificate(signingCertificate)
                .clientTransportCertificate(interpreter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE_NAME_V2))
                .clientSigningKeyId(interpreter.getUUID(CLIENT_SIGNING_KEY_ID_NAME_V2))
                .clientTransportKeyId(interpreter.getUUID(CLIENT_TRANSPORT_KEY_ID_NAME_V2))
                .clientEmail(interpreter.getValue(CLIENT_CONTACT_EMAIL))
                .clientWebsiteUri(interpreter.getValue(CLIENT_WEBSITE_URI))
                .clientLogoUri(interpreter.getValue(CLIENT_LOGO_URI))
                .signingKeyIdHeader(getCertificateUrl(signingCertificate, properties.getS3baseUrl()))
                .build();
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_TRANSPORT_KEY_ID_NAME_V2, CLIENT_TRANSPORT_CERTIFICATE_NAME_V2));
    }

    @Override
    public Optional<KeyRequirements> getSigningKeyRequirements() {
        return Optional.of(keyRequirementsProducer.produce(CLIENT_SIGNING_KEY_ID_NAME_V2, CLIENT_SIGNING_CERTIFICATE_NAME_V2));
    }

    private String getCertificateUrl(X509Certificate certificate, String s3BaseUrl) throws CertificateEncodingException {
        final String fingerprint = new Fingerprint(certificate.getEncoded()).toString();
        return s3BaseUrl + "/" + fingerprint + PEM_FORMAT_EXTENSION;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredTypedAuthMeans() {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> getRegisteredAuthMeans(ObjectNode registrationResponse) {
        Map<String, String> registeredAuthMeans = new HashMap<>();
        registeredAuthMeans.put(CLIENT_ID_STRING_NAME, registrationResponse.get("client_id").textValue());
        registeredAuthMeans.put(CLIENT_SECRET_STRING_NAME, registrationResponse.get("client_secret").textValue());
        registeredAuthMeans.put(CLIENT_REGISTRATION_ACCESS_TOKEN_NAME, registrationResponse.get("registration_access_token").textValue());
        return registeredAuthMeans;
    }
}
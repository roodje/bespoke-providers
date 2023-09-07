package com.yolt.providers.openbanking.ais.kbciegroup;

import com.yolt.providers.common.domain.authenticationmeans.AuthenticationMeanType;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import lombok.Getter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.*;
import static com.yolt.providers.openbanking.ais.kbciegroup.common.auth.KbcIeGroupAuthMeansBuilder.*;


@Getter
public class KbcIeGroupSampleAuthenticationMeans {

    private static final String CERTIFICATE_PATH = "certificates/fake/fake-eidas-certificate.pem";

    private static final String INSTITUTION_ID = "0015800001ZEZ3yAAH";
    private static final String SOFTWARE_ID = "someFakeSotwareId";
    private static final String CLIENT_ID = "someClientId";
    private static final String CLIENT_SECRET = "someClientSecret";
    private static final String SIGNING_KEY_HEADER_ID = "someSigningKeyHeaderId";
    private static final String SIGNING_KEY_ID = UUID.randomUUID().toString();
    private static final String TRANSPORT_KEY_ID = UUID.randomUUID().toString();
    private static final String CLIENT_NAME = "some client name";
    private static final String CLIENT_DESCRIPTION = "some client description";
    private static final String JWKS_ENDPOINT = "https://jwksuri.com";
    private static final String BUSINESS_CONTACT_NAME = "some Business Contact";
    private static final String BUSINESS_CONTACT_EMAIL = "someBusiness@contact.email";
    private static final String BUSINESS_CONTACT_PHONE = "+11123456789";
    private static final String TECHNICAL_CONTACT_NAME = "some Technical Contact";
    private static final String TECHNICAL_CONTACT_EMAIL = "someTechnical@contact.email";
    private static final String TECHNICAL_CONTACT_PHONE = "+11987654321";

    public Map<String, BasicAuthenticationMean> getKbcIeGroupSampleAuthenticationMeansForAutoonboarding() throws IOException, URISyntaxException {
        return getKbcIeGroupCommonAuthenticationMeans();
    }

    public Map<String, BasicAuthenticationMean> getKbcIeGroupSampleAuthenticationMeansForAis() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = getKbcIeGroupCommonAuthenticationMeans();

        authenticationMeans.put(CLIENT_ID_NAME, new BasicAuthenticationMean(CLIENT_ID_STRING.getType(), CLIENT_ID));
        authenticationMeans.put(CLIENT_SECRET_NAME, new BasicAuthenticationMean(CLIENT_SECRET_STRING.getType(), CLIENT_SECRET));

        return authenticationMeans;
    }

    private Map<String, BasicAuthenticationMean> getKbcIeGroupCommonAuthenticationMeans() throws IOException, URISyntaxException {
        Map<String, BasicAuthenticationMean> authenticationMeans = new HashMap<>();

        authenticationMeans.put(INSTITUTION_ID_NAME, new BasicAuthenticationMean(INSTITUTION_ID_STRING.getType(), INSTITUTION_ID));
        authenticationMeans.put(SOFTWARE_ID_NAME, new BasicAuthenticationMean(SOFTWARE_ID_STRING.getType(), SOFTWARE_ID));
        authenticationMeans.put(SIGNING_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, new BasicAuthenticationMean(CERTIFICATE_PEM.getType(), readFakeCertificatePem()));
        authenticationMeans.put(TRANSPORT_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), TRANSPORT_KEY_ID));
        authenticationMeans.put(SIGNING_PRIVATE_KEY_ID_NAME, new BasicAuthenticationMean(KEY_ID.getType(), SIGNING_KEY_ID));
        authenticationMeans.put(CLIENT_NAME_NAME, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Organisation name"), CLIENT_NAME));
        authenticationMeans.put(CLIENT_DESCRIPTION_NAME, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Application name"), CLIENT_DESCRIPTION));
        authenticationMeans.put(JWKS_ENDPOINT_NAME, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("URL of public JWKs endpoint"), JWKS_ENDPOINT));
        authenticationMeans.put(BUSINESS_CONTACT_NAME_NAME, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Business contact name"), BUSINESS_CONTACT_NAME));
        authenticationMeans.put(BUSINESS_CONTACT_EMAIL_NAME, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Business contact e-mail"), BUSINESS_CONTACT_EMAIL));
        authenticationMeans.put(BUSINESS_CONTACT_PHONE_NAME, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Business contact phone number"), BUSINESS_CONTACT_PHONE));
        authenticationMeans.put(TECHNICAL_CONTACT_NAME_NAME, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Technical contact name"), TECHNICAL_CONTACT_NAME));
        authenticationMeans.put(TECHNICAL_CONTACT_EMAIL_NAME, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Technical contact e-mail"), TECHNICAL_CONTACT_EMAIL));
        authenticationMeans.put(TECHNICAL_CONTACT_PHONE_NAME, new BasicAuthenticationMean(getCustomizedAuthenticationMeanType("Technical contact phone number"), TECHNICAL_CONTACT_PHONE));
        return authenticationMeans;
    }

    public String readFakeCertificatePem() throws URISyntaxException, IOException {
        URL certificateUrl = this.getClass().getClassLoader().getResource(CERTIFICATE_PATH);
        return new String(Files.readAllBytes(Paths.get(Objects.requireNonNull(certificateUrl).toURI())));
    }

    private AuthenticationMeanType getCustomizedAuthenticationMeanType(String displayName) {
        return new TypedAuthenticationMeans(displayName, NoWhiteCharacterStringType.getInstance(), ONE_LINE_STRING).getType();
    }
}
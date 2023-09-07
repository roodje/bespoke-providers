package com.yolt.providers.bancatransilvania.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.AuthenticationMeanType;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.Data;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;

@Data
public class BancaTransilvaniaGroupAuthenticationMeansProducerV1 implements BancaTransilvaniaGroupAuthenticationMeansProducer {

    private static final String COUNTRY = "C";
    private static final String ORGANIZATION_NAME = "O";
    private static final String ORGANIZATION_UNIT = "OU";
    private static final String COMMON_NAME = "CN";
    private static final String STATE = "ST";
    private static final String LOCALITY_NAME = "L";

    public static final String CLIENT_NAME = "client-name";
    public static final String CLIENT_COMPANY_NAME = "client-company-name";
    public static final String CLIENT_WEBSITE_URI_NAME = "client-website-uri";
    public static final String CLIENT_CONTACT_NAME = "client-contact";
    public static final String CLIENT_EMAIL_NAME = "client-email";
    public static final String CLIENT_PHONE_NAME = "client-phone";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";

    public static final TypedAuthenticationMeans CLIENT_NAME_TYPE = createTypeAuthMean("Client name", StringType.getInstance());
    public static final TypedAuthenticationMeans CLIENT_COMPANY_NAME_TYPE = createTypeAuthMean("Client company name", StringType.getInstance());
    public static final TypedAuthenticationMeans CLIENT_WEBSITE_URI_TYPE = createTypeAuthMean("Client website uri (URL encoded)", StringType.getInstance());
    public static final TypedAuthenticationMeans CLIENT_CONTACT_NAME_TYPE = createTypeAuthMean("Contact name", StringType.getInstance());
    public static final TypedAuthenticationMeans CLIENT_PHONE_TYPE = createTypeAuthMean("Client phone number", NoWhiteCharacterStringType.getInstance());

    private static TypedAuthenticationMeans createTypeAuthMean(String displayName, AuthenticationMeanType authMeanType) {
        return new TypedAuthenticationMeans(displayName, authMeanType, ONE_LINE_STRING);
    }

    @Override
    public Optional<KeyRequirements> getTransportKeyRequirements() {
        return Optional.of(new KeyRequirements(getKeyRequirements(), TRANSPORT_KEY_ID_NAME, TRANSPORT_CERTIFICATE_NAME));
    }

    private static KeyMaterialRequirements getKeyRequirements() {
        Set<KeyAlgorithm> supportedAlgorithms = new HashSet<>();
        supportedAlgorithms.add(KeyAlgorithm.RSA2048);
        supportedAlgorithms.add(KeyAlgorithm.RSA4096);

        Set<SignatureAlgorithm> supportedSignatureAlgorithms = new HashSet<>();
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA256_WITH_RSA);
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA512_WITH_RSA);

        List<DistinguishedNameElement> requiredDNs = new ArrayList<>();
        requiredDNs.add(new DistinguishedNameElement(COUNTRY, "", "Country", true));
        requiredDNs.add(new DistinguishedNameElement(STATE, "", "State / Province", true));
        requiredDNs.add(new DistinguishedNameElement(LOCALITY_NAME, "", "Locality name", true));
        requiredDNs.add(new DistinguishedNameElement(ORGANIZATION_NAME, "", "Organization name", true));
        requiredDNs.add(new DistinguishedNameElement(ORGANIZATION_UNIT, "", "Organizational unit", true));
        requiredDNs.add(new DistinguishedNameElement(COMMON_NAME, "", "Common name", true));
        return new KeyMaterialRequirements(supportedAlgorithms, supportedSignatureAlgorithms, requiredDNs);
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(CLIENT_NAME, CLIENT_NAME_TYPE);
        typedAuthMeans.put(CLIENT_COMPANY_NAME, CLIENT_COMPANY_NAME_TYPE);
        typedAuthMeans.put(CLIENT_WEBSITE_URI_NAME, CLIENT_WEBSITE_URI_TYPE);
        typedAuthMeans.put(CLIENT_CONTACT_NAME, CLIENT_CONTACT_NAME_TYPE);
        typedAuthMeans.put(CLIENT_EMAIL_NAME, TypedAuthenticationMeans.CLIENT_EMAIL);
        typedAuthMeans.put(CLIENT_PHONE_NAME, CLIENT_PHONE_TYPE);
        typedAuthMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        return typedAuthMeans;
    }

    @Override
    public Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthMeans = new HashMap<>();
        typedAuthMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        return typedAuthMeans;
    }

    @Override
    public BancaTransilvaniaGroupAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans,
                                                                               String providerIdentifier) {
        return BancaTransilvaniaGroupAuthenticationMeans.builder()
                .clientName(getValue(authMeans, providerIdentifier, CLIENT_NAME))
                .clientCompanyName(getValue(authMeans, providerIdentifier, CLIENT_COMPANY_NAME))
                .clientWebsiteUrl(getValue(authMeans, providerIdentifier, CLIENT_WEBSITE_URI_NAME))
                .clientContactPerson(getValue(authMeans, providerIdentifier, CLIENT_CONTACT_NAME))
                .clientEmail(getValue(authMeans, providerIdentifier, CLIENT_EMAIL_NAME))
                .clientPhoneNumber(getValue(authMeans, providerIdentifier, CLIENT_PHONE_NAME))
                .transportCertificate(getCertificate(authMeans, providerIdentifier, TRANSPORT_CERTIFICATE_NAME))
                .transportKeyId(getUUID(authMeans, providerIdentifier, TRANSPORT_KEY_ID_NAME))
                .clientId(getNullableValue(authMeans, CLIENT_ID_NAME))
                .clientSecret(getNullableValue(authMeans, CLIENT_SECRET_NAME))
                .build();
    }

    private static X509Certificate getCertificate(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier, String authKey) {
        String authValue = getValue(authMeans, providerIdentifier, authKey);
        try {
            return KeyUtil.createCertificateFromPemFormat(authValue);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, authKey, "Cannot parse authentication mean to X509 certificate");
        }
    }

    private static UUID getUUID(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier, String authKey) {
        String authValue = getValue(authMeans, providerIdentifier, authKey);
        try {
            return UUID.fromString(authValue);
        } catch (Exception e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, authKey, "Cannot parse authentication mean to UUID");
        }
    }

    private static String getValue(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier, String authKey) {
        String authValue = getNullableValue(authMeans, authKey);
        if (authValue == null) {
            throw new MissingAuthenticationMeansException(providerIdentifier, authKey);
        }
        return authValue;
    }

    private static String getNullableValue(Map<String, BasicAuthenticationMean> authMeans, String authKey) {
        if (authMeans.containsKey(authKey)) {
            return authMeans.get(authKey).getValue();
        }
        return null;
    }
}

package com.yolt.providers.deutschebank.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@RequiredArgsConstructor
public class DeutscheBankGroupAuthenticationMeansProducerV1 implements DeutscheBankGroupAuthenticationMeansProducer {

    private static final String COUNTRY = "C";
    private static final String ORGANIZATION_NAME = "O";
    private static final String ORGANIZATION_UNIT = "OU";
    private static final String COMMON_NAME = "CN";
    private static final String STATE = "ST";
    private static final String LOCALITY_NAME = "L";

    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";

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
        typedAuthMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        return typedAuthMeans;
    }

    @Override
    public DeutscheBankGroupAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authMeans,
                                                                          String providerIdentifier) {
        return DeutscheBankGroupAuthenticationMeans.builder()
                .transportCertificate(getCertificate(authMeans, providerIdentifier, TRANSPORT_CERTIFICATE_NAME))
                .transportKeyId(getValue(authMeans, providerIdentifier, TRANSPORT_KEY_ID_NAME))
                .build();
    }

    private static X509Certificate getCertificate(Map<String, BasicAuthenticationMean> authMeans, String providerIdentifier, String authKey) {
        String authValue = getValue(authMeans, providerIdentifier, authKey);
        try {
            return KeyUtil.createCertificateFromPemFormat(authValue);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(providerIdentifier, authKey, "Cannot process certificate for thumbprint");
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

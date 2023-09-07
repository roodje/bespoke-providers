package com.yolt.providers.openbanking.ais.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bouncycastle.util.encoders.DecoderException;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HsmUtils {

    private static final String COUNTRY = "C";
    private static final String ORGANIZATION_NAME = "O";
    private static final String ORGANIZATION_UNIT = "OU";
    private static final String COMMON_NAME = "CN";

    public static Optional<KeyRequirements> getKeyRequirements(final String keyIdName) {
        return getKeyRequirements(keyIdName, null);
    }

    public static Optional<KeyRequirements> getKeyRequirements(String keyIdName,
                                                               String keyReference) {
        KeyRequirements keyRequirements = new KeyRequirements(getKeyRequirements(), keyIdName, keyReference);
        return Optional.of(keyRequirements);
    }

    private static KeyMaterialRequirements getKeyRequirements() {
        Set<KeyAlgorithm> supportedAlgorithms = new HashSet<>();
        supportedAlgorithms.add(KeyAlgorithm.RSA2048);
        supportedAlgorithms.add(KeyAlgorithm.RSA4096);

        Set<SignatureAlgorithm> supportedSignatureAlgorithms = new HashSet<>();
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA256_WITH_RSA);

        List<DistinguishedNameElement> requiredDNs = new ArrayList<>();
        requiredDNs.add(new DistinguishedNameElement(COUNTRY, "GB", "", false));
        requiredDNs.add(new DistinguishedNameElement(ORGANIZATION_NAME, "OpenBanking", "", false));
        requiredDNs.add(new DistinguishedNameElement(ORGANIZATION_UNIT));
        requiredDNs.add(new DistinguishedNameElement(COMMON_NAME));

        return new KeyMaterialRequirements(supportedAlgorithms, supportedSignatureAlgorithms, requiredDNs);
    }

    public static X509Certificate getCertificate(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                 final String certificateMean,
                                                 final String providerKey) {

        try {
            final String certificateString = authenticationMeans.get(certificateMean).getValue();
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (DecoderException | CertificateException e) {
            throw new InvalidAuthenticationMeansException(
                    providerKey,
                    certificateMean,
                    "Cannot process certificate");
        }
    }
}

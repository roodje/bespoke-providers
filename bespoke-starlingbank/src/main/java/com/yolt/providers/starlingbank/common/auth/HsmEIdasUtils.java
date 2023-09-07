package com.yolt.providers.starlingbank.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HsmEIdasUtils {

    private static final String COUNTRY = "C";
    private static final String ORGANIZATION_NAME = "O";
    private static final String ORGANIZATION_UNIT = "OU";
    private static final String COMMON_NAME = "CN";
    private static final String STATE = "ST";
    private static final String LOCALITY_NAME = "L";

    public static Optional<KeyRequirements> getKeyRequirements(final String keyIdName) {
        return getKeyRequirements(keyIdName, null);
    }

    public static Optional<KeyRequirements> getKeyRequirements(final String keyIdName, final String certificateName) {
        KeyRequirements keyRequirements = new KeyRequirements(getKeyRequirements(), keyIdName, certificateName);
        return Optional.of(keyRequirements);
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
}

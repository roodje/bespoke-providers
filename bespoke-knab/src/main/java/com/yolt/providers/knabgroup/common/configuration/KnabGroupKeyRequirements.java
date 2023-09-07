package com.yolt.providers.knabgroup.common.configuration;

import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.securityutils.signing.SignatureAlgorithm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KnabGroupKeyRequirements {

    public static final KeyMaterialRequirements KNAB_GROUP_KEY_REQUIREMENTS = getKeyRequirements();

    private static KeyMaterialRequirements getKeyRequirements() {
        Set<KeyAlgorithm> supportedAlgorithms = new HashSet<>();
        supportedAlgorithms.add(KeyAlgorithm.RSA2048);
        supportedAlgorithms.add(KeyAlgorithm.RSA4096);

        Set<SignatureAlgorithm> supportedSignatureAlgorithms = new HashSet<>();
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA256_WITH_RSA);
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA512_WITH_RSA);

        List<DistinguishedNameElement> requiredDNs = new ArrayList<>();
        requiredDNs.add(new DistinguishedNameElement("C", "", "Country", true));
        requiredDNs.add(new DistinguishedNameElement("ST", "", "State / Province", true));
        requiredDNs.add(new DistinguishedNameElement("L", "", "Locality name", true));
        requiredDNs.add(new DistinguishedNameElement("O", "", "Organization name", true));
        requiredDNs.add(new DistinguishedNameElement("OU", "", "Organizational unit", true));
        requiredDNs.add(new DistinguishedNameElement("CN", "", "Common name", true));

        return new KeyMaterialRequirements(supportedAlgorithms, supportedSignatureAlgorithms, requiredDNs);
    }
}

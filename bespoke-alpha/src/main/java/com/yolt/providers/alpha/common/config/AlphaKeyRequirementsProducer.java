package com.yolt.providers.alpha.common.config;

import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.securityutils.signing.SignatureAlgorithm;

import java.util.*;

public class AlphaKeyRequirementsProducer {
    public Optional<KeyRequirements> getKeyRequirements(String privateKid, String publicKey) {
        return Optional.of(new KeyRequirements(getKeyRequirements(), privateKid, publicKey));
    }

    private KeyMaterialRequirements getKeyRequirements() {
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

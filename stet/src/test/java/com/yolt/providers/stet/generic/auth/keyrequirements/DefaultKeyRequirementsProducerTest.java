package com.yolt.providers.stet.generic.auth.keyrequirements;

import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultKeyRequirementsProducerTest {

    @Test
    void shouldProduceEIDASKeyRequirements() {
        // given
        String clientKeyIdName = "client-key-id";
        String clientCertificateName = "client-certificate-name";
        KeyRequirementsProducer keyRequirementsProducer = new DefaultKeyRequirementsProducer();

        // when
        KeyRequirements keyRequirements = keyRequirementsProducer.produce(clientKeyIdName, clientCertificateName);

        // then
        assertThat(keyRequirements.getPrivateKidAuthenticationMeanReference()).isEqualTo(clientKeyIdName);
        assertThat(keyRequirements.getPublicKeyAuthenticationMeanReference()).isEqualTo(clientCertificateName);

        KeyMaterialRequirements keyMaterialRequirements = keyRequirements.getKeyRequirements();

        Set<KeyAlgorithm> keyAlgorithms = keyMaterialRequirements.getKeyAlgorithms();
        assertThat(keyAlgorithms).containsExactlyInAnyOrder(KeyAlgorithm.RSA2048, KeyAlgorithm.RSA4096);

        Set<SignatureAlgorithm> signatureAlgorithms = keyMaterialRequirements.getSignatureAlgorithms();
        assertThat(signatureAlgorithms).containsExactlyInAnyOrder(SignatureAlgorithm.SHA256_WITH_RSA, SignatureAlgorithm.SHA512_WITH_RSA);

        List<DistinguishedNameElement> distinguishedNames = keyMaterialRequirements.getDistinguishedNames();
        assertThat(distinguishedNames).containsExactly(
                new DistinguishedNameElement("C", "", "Country", true),
                new DistinguishedNameElement("ST", "", "State / Province", true),
                new DistinguishedNameElement("L", "", "Locality name", true),
                new DistinguishedNameElement("O", "", "Organization name", true),
                new DistinguishedNameElement("OU", "", "Organizational unit", true),
                new DistinguishedNameElement("CN", "", "Common name", true));
    }
}

package com.yolt.providers.axabanque.common.requirements;

import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm.RSA2048;
import static com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm.RSA4096;
import static com.yolt.securityutils.signing.SignatureAlgorithm.SHA256_WITH_RSA;
import static com.yolt.securityutils.signing.SignatureAlgorithm.SHA512_WITH_RSA;
import static org.assertj.core.api.Assertions.assertThat;

public class TransportKeyRequirementsProducerTest {

    @Test
    public void shouldReturnRequirements() {
        //given
        TransportKeyRequirementsProducer transportKeyRequirementsProducer = new TransportKeyRequirementsProducer();
        //when
        KeyRequirements requirements = transportKeyRequirementsProducer.getRequirements().get();
        //then
        List<String> distinguishNameTypes = requirements.getKeyRequirements().getDistinguishedNames().stream().map(DistinguishedNameElement::getType).collect(Collectors.toList());
        assertThat(distinguishNameTypes).containsExactlyInAnyOrder("C", "ST", "L", "O", "OU", "CN");

        assertThat(requirements.getKeyRequirements().getKeyAlgorithms()).containsExactlyInAnyOrder(RSA2048, RSA4096);

        assertThat(requirements.getKeyRequirements().getSignatureAlgorithms()).containsExactlyInAnyOrder(SHA256_WITH_RSA, SHA512_WITH_RSA);
    }
}
package com.yolt.providers.yoltprovider.pis.sepa;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SignatureDTOTest {

    @Test
    public void shouldReturnSignatureHeaderWithOnlyAlgorithmForGetSignatureHeaderWhenKeyIdAndSignatureAreNotProvided() {
        // given
        SignatureDTO signatureDTO = new SignatureDTO(null, null);

        // when
        String signature = signatureDTO.getSignatureHeader();

        // then
        assertThat(signature).isEqualTo("\"algorithm=\"" + SigningUtils.SIGNATURE_ALGORITHM.getJvmAlgorithm() + "\"\"");
    }

    @Test
    public void shouldReturnSignatureHeaderWithKeyIdAndAlgorithmForGetSigningSignatureHeaderWhenSignatureIsNotProvided() {
        // given
        UUID kid = UUID.randomUUID();
        SignatureDTO signatureDTO = new SignatureDTO(kid, null);

        // when
        String signature = signatureDTO.getSignatureHeader();

        // then
        assertThat(signature).isEqualTo("\"keyId=\"" + kid.toString() + "\",algorithm=\""
                + SigningUtils.SIGNATURE_ALGORITHM.getJvmAlgorithm() + "\"\"");
    }

    @Test
    public void shouldReturnSignatureHeaderWithAlgorithmAndSignatureForGetSignatureHeaderWhenKeyIdIsNotProvided() {
        // given
        String expected = "some_signature";
        SignatureDTO signatureDTO = new SignatureDTO(null, expected);

        // when
        String signature = signatureDTO.getSignatureHeader();

        // then
        assertThat(signature).isEqualTo("\"algorithm=\"" + SigningUtils.SIGNATURE_ALGORITHM.getJvmAlgorithm()
                + "\",signature=\"" + expected + "\"\"");
    }

    @Test
    public void shouldReturnFullCorrectSignatureHeaderForGetSignatureHeaderWhenKeyIdAndSignatureAreProvided() {
        // given
        UUID kid = UUID.randomUUID();
        String expected = "some_signature";
        SignatureDTO signatureDTO = new SignatureDTO(kid, expected);

        // when
        String signature = signatureDTO.getSignatureHeader();

        // then
        assertThat(signature).isEqualTo("\"keyId=\"" + kid.toString() + "\",algorithm=\""
                + SigningUtils.SIGNATURE_ALGORITHM.getJvmAlgorithm() + "\",signature=\"" + expected + "\"\"");
    }
}

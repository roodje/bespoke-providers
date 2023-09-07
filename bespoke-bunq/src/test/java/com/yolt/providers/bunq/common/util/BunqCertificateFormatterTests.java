package com.yolt.providers.bunq.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class BunqCertificateFormatterTests {

    BunqCertificateFormatter subject = new BunqCertificateFormatter();

    @Test
    public void shouldReturnFormattedCertificate() {
        //given
        String givenCertificate = "-----BEGIN CERTIFICATE-----" +
                "cert1" +
                "-----END CERTIFICATE-----";
        String expectedCertificate = "-----BEGIN CERTIFICATE-----\n" +
                "cert1\n" +
                "-----END CERTIFICATE-----\n";
        //when
        String formattedCertificate = subject.formatCertificateString(givenCertificate);
        //then
        assertThat(formattedCertificate).isEqualTo(expectedCertificate);
    }

    @Test
    public void shouldReturnCutChain() {
        String givenCertificateChain = "-----BEGIN CERTIFICATE-----" +
                "cert1" +
                "-----END CERTIFICATE-----" +
                "-----BEGIN CERTIFICATE-----" +
                "cert2" +
                "-----END CERTIFICATE-----" +
                "-----BEGIN CERTIFICATE-----" +
                "cert3" +
                "-----END CERTIFICATE-----" +
                "-----BEGIN CERTIFICATE-----" +
                "cert4" +
                "-----END CERTIFICATE-----";
        String expectedCertificateChain = "-----BEGIN CERTIFICATE-----" +
                "cert1" +
                "-----END CERTIFICATE-----" +
                "-----BEGIN CERTIFICATE-----" +
                "cert2" +
                "-----END CERTIFICATE-----";
        int wantedNumberOfCertsAfterCutInChain = 2;
        //when
        String formattedCertificateChain = subject.retainWantedNumberCertificatesInChain(givenCertificateChain, wantedNumberOfCertsAfterCutInChain);
        //then
        assertThat(formattedCertificateChain).isEqualTo(expectedCertificateChain);
    }

    @Test
    public void shouldReturnFormattedCertificateChain() {
        String givenCertificateChain = "-----BEGIN CERTIFICATE-----\n" +
                "cert1a\n" +
                "cert1b\n" +
                "-----END CERTIFICATE-----\n" +
                "-----BEGIN CERTIFICATE-----\n" +
                "cert2a\n" +
                "cert2b\n" +
                "-----END CERTIFICATE-----\n";
        String expectedCertificateChain = "-----BEGIN CERTIFICATE-----\n" +
                "cert1a" +
                "cert1b\n" +
                "-----END CERTIFICATE-----\n" +
                "-----BEGIN CERTIFICATE-----\n" +
                "cert2a" +
                "cert2b\n" +
                "-----END CERTIFICATE-----\n";
        //when
        String formattedCertificateChain = subject.removeNewLinesAndAddNewOnesInProperPlaceInCertificateOrChain(givenCertificateChain);
        //then
        assertThat(formattedCertificateChain).isEqualTo(expectedCertificateChain);
    }

    @Test
    public void shouldReturnCertificateChainWithoutLeaf() {
        String givenCertificateChain = "-----BEGIN CERTIFICATE-----" +
                "cert1" +
                "-----END CERTIFICATE-----" +
                "-----BEGIN CERTIFICATE-----" +
                "cert2" +
                "-----END CERTIFICATE-----" +
                "-----BEGIN CERTIFICATE-----" +
                "cert3" +
                "-----END CERTIFICATE-----" +
                "-----BEGIN CERTIFICATE-----" +
                "cert4" +
                "-----END CERTIFICATE-----";
        String expectedCertificateChain = "-----BEGIN CERTIFICATE-----" +
                "cert2" +
                "-----END CERTIFICATE-----" +
                "-----BEGIN CERTIFICATE-----" +
                "cert3" +
                "-----END CERTIFICATE-----" +
                "-----BEGIN CERTIFICATE-----" +
                "cert4" +
                "-----END CERTIFICATE-----";
        //when
        String formattedCertificateChain = subject.removeLeafCertificateFromChain(givenCertificateChain);
        //then
        assertThat(formattedCertificateChain).isEqualTo(expectedCertificateChain);

    }

}

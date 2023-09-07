package com.yolt.providers.bunq.common.util;

import org.springframework.util.StringUtils;

public class BunqCertificateFormatter {

    private static final String PUBLIC_CERTIFICATE_FORMAT = "-----BEGIN CERTIFICATE-----%s-----END CERTIFICATE-----\n";
    private static final String BEGIN_CERTIFICATE_PATTERN = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE_PATTERN = "-----END CERTIFICATE-----";
    private static final String BEGIN_CERTIFICATE_FORMATTED_PATTERN = "-----BEGIN CERTIFICATE-----\n";
    private static final String END_CERTIFICATE_FORMATTED_PATTERN = "\n-----END CERTIFICATE-----\n";

    public String formatCertificateString(String certificate) {
        String formattedCertificate = certificate;
        if (!formattedCertificate.contains(BEGIN_CERTIFICATE_PATTERN)) {
            formattedCertificate = String.format(PUBLIC_CERTIFICATE_FORMAT, formattedCertificate);
        }
        return removeNewLinesAndAddNewOnesInProperPlaceInCertificateOrChain(formattedCertificate);
    }

    public String removeNewLinesAndAddNewOnesInProperPlaceInCertificateOrChain(String toFormat) {
        return toFormat.replaceAll("\\R", "")
                .replace(BEGIN_CERTIFICATE_PATTERN, BEGIN_CERTIFICATE_FORMATTED_PATTERN)
                .replace(END_CERTIFICATE_PATTERN, END_CERTIFICATE_FORMATTED_PATTERN);

    }

    public String retainWantedNumberCertificatesInChain(String fullCertificateChain, int wantedNumberOfCertsAfterCutInChain) {
        String certificateChainToCut = fullCertificateChain;
        while (StringUtils.countOccurrencesOf(certificateChainToCut, BEGIN_CERTIFICATE_PATTERN) > wantedNumberOfCertsAfterCutInChain) {
            certificateChainToCut = certificateChainToCut.substring(0, certificateChainToCut.lastIndexOf(BEGIN_CERTIFICATE_PATTERN));
        }
        return certificateChainToCut;
    }

    public String removeLeafCertificateFromChain(String fullCertificateChain) {
        return fullCertificateChain.substring(fullCertificateChain.indexOf(BEGIN_CERTIFICATE_PATTERN, BEGIN_CERTIFICATE_PATTERN.length()));
    }

}

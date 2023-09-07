package com.yolt.providers.openbanking.ais.hsbcgroup.common.http;

import org.bouncycastle.asn1.x500.X500Name;

import javax.security.auth.x500.X500Principal;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;

public class HsbcGroupCertificateIdentityExtractor implements Function<X509Certificate, String> {

    @Override
    public String apply(X509Certificate certificate) {
        X500Principal subjectPrincipal = certificate.getSubjectX500Principal();
        String subjectPrincipalFormattedByRFC2253 = subjectPrincipal.getName();
        return Arrays.asList(subjectPrincipalFormattedByRFC2253.split(","))
                .stream()
                .map(this::formatRelativeDistinguishedName)
                .collect(Collectors.joining(","));
    }

    private String formatRelativeDistinguishedName(String rdn) {
        String[] pair = rdn.split("=");

        if (pair.length != 2)
            return rdn;

        switch (pair[0]) {
            case "STREET":
                return replaceRdnKey(rdn, "STREET", "street");
            case "2.5.4.5":
                return replaceRdnKey(rdn, "SERIALNUMBER", "serialNumber");
            case "2.5.4.15":
                return replaceRdnKey(rdn, "BusinessCategory", "2.5.4.15");
            case "2.5.4.17":
                return replaceRdnKey(rdn, "PostalCode", "2.5.4.17");
            case "2.5.4.97":
                return rdn.replace(pair[1], pair[1].toUpperCase());
            default:
        }

        if (pair[1].startsWith("#")) {
            return new X500Name(rdn).toString();
        }

        return rdn;
    }

    private String replaceRdnKey(String rdn, String from, String to) {
        return new X500Name(rdn).toString().replace(from, to);
    }

}

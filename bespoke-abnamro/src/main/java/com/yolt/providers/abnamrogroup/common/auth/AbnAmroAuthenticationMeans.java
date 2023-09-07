package com.yolt.providers.abnamrogroup.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.Data;
import lombok.Getter;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Getter
@Data
public class AbnAmroAuthenticationMeans {
    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_TRANSPORT_KEY_ID = "client-transport-private-key-id";
    public static final String CLIENT_TRANSPORT_CERTIFICATE = "client-transport-certificate";
    public static final String API_KEY_NAME = "api-key";

    private final String clientId;
    private final UUID clientTransportKid;
    private final X509Certificate clientTransportCertificate;
    private final String apiKey;

    public AbnAmroAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        try {
            this.clientId = typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue();
            this.clientTransportKid = UUID.fromString(typedAuthenticationMeans.get(CLIENT_TRANSPORT_KEY_ID).getValue());
            this.clientTransportCertificate = KeyUtil.createCertificateFromPemFormat(typedAuthenticationMeans.get(CLIENT_TRANSPORT_CERTIFICATE).getValue());
            this.apiKey = typedAuthenticationMeans.get(API_KEY_NAME).getValue();
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException("ABN Amro",
                    CLIENT_TRANSPORT_CERTIFICATE, "Cannot process certificate for thumbprint");
        }
    }

    public static Map<String, TypedAuthenticationMeans> getStringTypedAuthenticationMeansMapForAisAndPis() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeansMap.put(CLIENT_TRANSPORT_KEY_ID, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeansMap.put(CLIENT_TRANSPORT_CERTIFICATE, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeansMap.put(API_KEY_NAME, TypedAuthenticationMeans.API_KEY_STRING);

        return typedAuthenticationMeansMap;
    }

    public static Optional<KeyRequirements> getTransportKeyRequirements() {
        KeyMaterialRequirements keyRequirements = getKeyMaterialRequirements();

        KeyRequirements transportKeyRequirements = new KeyRequirements(keyRequirements, CLIENT_TRANSPORT_KEY_ID, CLIENT_TRANSPORT_CERTIFICATE);
        return Optional.of(transportKeyRequirements);
    }

    private static KeyMaterialRequirements getKeyMaterialRequirements() {
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

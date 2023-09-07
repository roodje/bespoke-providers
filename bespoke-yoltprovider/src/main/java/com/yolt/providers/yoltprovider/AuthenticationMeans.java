package com.yolt.providers.yoltprovider;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.RenderingType;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.PemType;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.domain.authenticationmeans.types.UuidType;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Getter
@RequiredArgsConstructor
public class AuthenticationMeans {

    public static final String CLIENT_ID = "client-id";
    public static final String CLIENT_SECRET = "client-secret";
    public static final String CLIENT_SIGNING_PRIVATE_KEY_ID = "client-signing-private-key-id";
    public static final String CLIENT_TRANSPORT_PRIVATE_KEY_ID = "client-transport-private-key-id";
    public static final String CLIENT_SIGNING_CERTIFICATE = "client-signing-certificate";
    public static final String CLIENT_TRANSPORT_CERTIFICATE = "client-transport-certificate";

    private final String clientId;
    private final String clientSecret;
    private final X509Certificate clientTransportCertificate;
    private final X509Certificate clientSigningCertificate;
    private final UUID signingKid;
    private final UUID transportKid;

    static AuthenticationMeans fromAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        String clientId = typedAuthenticationMeans.get(CLIENT_ID).getValue();
        String clientSecret = typedAuthenticationMeans.get(CLIENT_SECRET).getValue();
        UUID signingKid = UUID.fromString(typedAuthenticationMeans.get(CLIENT_SIGNING_PRIVATE_KEY_ID).getValue());
        UUID transportKid = UUID.fromString(typedAuthenticationMeans.get(CLIENT_TRANSPORT_PRIVATE_KEY_ID).getValue());
        X509Certificate clientSigningCertificate = createCertificate(typedAuthenticationMeans.get(CLIENT_SIGNING_CERTIFICATE).getValue());
        X509Certificate clientTransportCertificate = createCertificate(typedAuthenticationMeans.get(CLIENT_TRANSPORT_CERTIFICATE).getValue());

        return new AuthenticationMeans(clientId, clientSecret, clientTransportCertificate, clientSigningCertificate, signingKid, transportKid);
    }

    private static X509Certificate createCertificate(final String certificateString) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new RuntimeException("Cannot create x509 certificate from " +certificateString);
        }
    }


    public static Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> authenticationMeans = new HashMap<>();
        authenticationMeans.put(CLIENT_ID,
                new TypedAuthenticationMeans("Client Id (anything suffices for this test bank)", StringType.getInstance(), RenderingType.ONE_LINE_STRING)
        );
        authenticationMeans.put(CLIENT_SECRET,
                new TypedAuthenticationMeans("Client Secret (anything suffices for this test bank)", StringType.getInstance(), RenderingType.ONE_LINE_STRING)
        );
        // This is hidden on the screen, because it's returned as 'getSigningKeyRequirements()'.
        authenticationMeans.put(CLIENT_SIGNING_PRIVATE_KEY_ID,
                new TypedAuthenticationMeans("Signing Private Key Id", UuidType.getInstance(), RenderingType.ONE_LINE_STRING)
        );
        authenticationMeans.put(CLIENT_SIGNING_CERTIFICATE,
                new TypedAuthenticationMeans("Client Signing Certificate (PEM). Obtained by letting yoltbank sign the csr.", PemType.getInstance(), RenderingType.MULTI_LINE_STRING)
        );

        authenticationMeans.put(CLIENT_TRANSPORT_PRIVATE_KEY_ID,
                new TypedAuthenticationMeans("Key id of transport certificate.",
                        UuidType.getInstance(), RenderingType.ONE_LINE_STRING)
        );
        authenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE,
                new TypedAuthenticationMeans("Client Transport Certificate (PEM). Obtained by letting yoltbank sign the csr.",
                        PemType.getInstance(), RenderingType.MULTI_LINE_STRING)
        );

        return authenticationMeans;
    }

    public static Optional<KeyRequirements> getSigningKeyRequirements() {
        final KeyRequirements signingRequirements = new KeyRequirements(getKeyRequirements(), CLIENT_SIGNING_PRIVATE_KEY_ID,
                CLIENT_SIGNING_CERTIFICATE);
        return Optional.of(signingRequirements);
    }

    public static Optional<KeyRequirements> getTransportKeyRequirements() {
        KeyRequirements transportRequirements = new KeyRequirements(getKeyRequirements(),
                CLIENT_TRANSPORT_PRIVATE_KEY_ID, CLIENT_TRANSPORT_CERTIFICATE);
        return Optional.of(transportRequirements);
    }

    private static KeyMaterialRequirements getKeyRequirements() {
        final Set<KeyAlgorithm> supportedAlgorithms = new HashSet<>();
        supportedAlgorithms.add(KeyAlgorithm.RSA2048);
        supportedAlgorithms.add(KeyAlgorithm.RSA4096);

        final Set<SignatureAlgorithm> supportedSignatureAlgorithms = new HashSet<>();
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA256_WITH_RSA);

        final List<DistinguishedNameElement> requiredDNs = new ArrayList<>();
        requiredDNs.add(new DistinguishedNameElement("C"));
        requiredDNs.add(new DistinguishedNameElement("O"));
        requiredDNs.add(new DistinguishedNameElement("OU"));
        requiredDNs.add(new DistinguishedNameElement("CN"));

        return new KeyMaterialRequirements(supportedAlgorithms, supportedSignatureAlgorithms, requiredDNs);
    }



}

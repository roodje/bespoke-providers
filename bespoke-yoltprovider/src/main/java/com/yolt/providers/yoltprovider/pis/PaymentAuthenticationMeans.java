package com.yolt.providers.yoltprovider.pis;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.RenderingType;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.providers.common.domain.authenticationmeans.types.UuidType;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;

@Getter
@RequiredArgsConstructor
@EqualsAndHashCode
public class PaymentAuthenticationMeans {

    public static final String CLIENT_ID = "client-id";
    public static final String PRIVATE_KID = "client-signing-private-keyid";
    public static final String PUBLIC_KID = "client-public-keyid";

    private final UUID clientId;
    private final UUID signingKid;
    private final UUID publicKid;

    public static PaymentAuthenticationMeans fromAuthenticationMeans(final Map<String, BasicAuthenticationMean> typedAuthenticationMeans) {
        UUID clientId = UUID.fromString(typedAuthenticationMeans.get(CLIENT_ID).getValue());
        UUID signingKid = UUID.fromString(typedAuthenticationMeans.get(PRIVATE_KID).getValue());
        UUID publicKid = UUID.fromString(typedAuthenticationMeans.get(PUBLIC_KID).getValue());

        return new PaymentAuthenticationMeans(clientId, signingKid, publicKid);
    }

    public static Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        final Map<String, TypedAuthenticationMeans> typedAuthenticationMeansMap = new HashMap<>();
        typedAuthenticationMeansMap.put(CLIENT_ID,
                new TypedAuthenticationMeans("Client Id (any uuid suffices)", NoWhiteCharacterStringType.getInstance(), RenderingType.ONE_LINE_STRING)
        );
        // This is hidden on the screen, because it's returned as 'getSigningKeyRequirements()'.
        typedAuthenticationMeansMap.put(PRIVATE_KID,
                new TypedAuthenticationMeans("Key Id", UuidType.getInstance(), RenderingType.ONE_LINE_STRING)
        );
        typedAuthenticationMeansMap.put(PUBLIC_KID,
                new TypedAuthenticationMeans("Key id of signed certificate. (obtained by letting yoltbank sign the csr through yoltbank/yolt-test-bank/pis/sepa/csr)",
                        UuidType.getInstance(), RenderingType.ONE_LINE_STRING)
        );
        return typedAuthenticationMeansMap;
    }

    public static Optional<KeyRequirements> getSigningKeyRequirements() {
        final KeyRequirements signingRequirements = new KeyRequirements(getKeyRequirements(), PRIVATE_KID);
        return Optional.of(signingRequirements);
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

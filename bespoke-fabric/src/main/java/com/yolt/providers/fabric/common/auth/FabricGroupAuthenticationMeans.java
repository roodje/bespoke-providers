package com.yolt.providers.fabric.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.DistinguishedNameElement;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyAlgorithm;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyMaterialRequirements;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.NoWhiteCharacterStringType;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;
import java.util.*;

import static com.yolt.providers.common.domain.authenticationmeans.RenderingType.ONE_LINE_STRING;
import static java.util.Collections.unmodifiableMap;

@Data
@Builder
public class FabricGroupAuthenticationMeans {

    public static final String ONBOARDING_URL = "onboarding-url";
    public static final String CLIENT_SIGNING_KEY_ID_NAME = "private-client-signing-key-id";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "private-client-transport-key-id";
    public static final String CLIENT_SIGNING_CERTIFICATE_NAME = "client-signing-certificate";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";

    private final String onboardingUrl;
    private final UUID signingKeyId;
    private final UUID transportKeyId;
    private final X509Certificate clientSigningCertificate;
    private final X509Certificate clientTransportCertificate;

    public static final Map<String, TypedAuthenticationMeans> typedClientConfiguration;

    static {
        Map<String, TypedAuthenticationMeans> typedClientConfigurationMap = new HashMap<>();
        typedClientConfigurationMap.put(CLIENT_SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedClientConfigurationMap.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedClientConfigurationMap.put(CLIENT_SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedClientConfigurationMap.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedClientConfigurationMap.put(ONBOARDING_URL, new TypedAuthenticationMeans("onboarding-url", NoWhiteCharacterStringType.getInstance(), ONE_LINE_STRING));
        typedClientConfiguration = unmodifiableMap(typedClientConfigurationMap);
    }

    private static final KeyMaterialRequirements keyMaterialRequirements;

    static {
        Set<KeyAlgorithm> supportedAlgorithms = new HashSet<>();
        supportedAlgorithms.add(KeyAlgorithm.RSA2048);
        supportedAlgorithms.add(KeyAlgorithm.RSA4096);

        Set<SignatureAlgorithm> supportedSignatureAlgorithms = new HashSet<>();
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA256_WITH_RSA);
        supportedSignatureAlgorithms.add(SignatureAlgorithm.SHA512_WITH_RSA);

        List<DistinguishedNameElement> distinguishedNameElements = new ArrayList<>();
        distinguishedNameElements.add(new DistinguishedNameElement("C", "", "Country", true));
        distinguishedNameElements.add(new DistinguishedNameElement("ST", "", "State / Province", true));
        distinguishedNameElements.add(new DistinguishedNameElement("L", "", "Locality name", true));
        distinguishedNameElements.add(new DistinguishedNameElement("O", "", "Organization name", true));
        distinguishedNameElements.add(new DistinguishedNameElement("OU", "", "Organizational unit", true));
        distinguishedNameElements.add(new DistinguishedNameElement("CN", "", "Common name", true));

        keyMaterialRequirements = new KeyMaterialRequirements(supportedAlgorithms, supportedSignatureAlgorithms, distinguishedNameElements);
    }

    public static final KeyRequirements signingKeyRequirements = new KeyRequirements(
            keyMaterialRequirements, CLIENT_SIGNING_KEY_ID_NAME, CLIENT_SIGNING_CERTIFICATE_NAME);
    public static final KeyRequirements transportKeyRequirements = new KeyRequirements(
            keyMaterialRequirements, CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME);

    public static FabricGroupAuthenticationMeans createAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                           final String providerIdentifier) {
        FabricGroupAuthenticationMeansAdapter adapter = new FabricGroupAuthenticationMeansAdapter(authenticationMeans, providerIdentifier);
        return FabricGroupAuthenticationMeans.builder()
                .clientTransportCertificate(adapter.getCertificate(CLIENT_TRANSPORT_CERTIFICATE_NAME))
                .clientSigningCertificate(adapter.getCertificate(CLIENT_SIGNING_CERTIFICATE_NAME))
                .signingKeyId(UUID.fromString(adapter.getValue(CLIENT_SIGNING_KEY_ID_NAME)))
                .transportKeyId(UUID.fromString(adapter.getValue(CLIENT_TRANSPORT_KEY_ID_NAME)))
                .onboardingUrl(adapter.getNullableValue(ONBOARDING_URL))
                .build();
    }
}

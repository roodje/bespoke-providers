package com.yolt.providers.bancacomercialaromana.common.auth;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.*;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.exception.MissingAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.securityutils.signing.SignatureAlgorithm;
import lombok.*;
import org.bouncycastle.util.Fingerprint;

import java.security.cert.*;
import java.util.*;

import static java.util.Collections.unmodifiableMap;

@Data
@RequiredArgsConstructor
public class BcrGroupAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "client-transport-certificate";
    public static final String CLIENT_SIGNING_CERTIFICATE_NAME = "client-signing-certificate";
    public static final String CLIENT_SIGNING_KEY_ID_NAME = "private-client-signing-key-id";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "private-client-transport-key-id";
    public static final String WEB_API_KEY_NAME = "web-api-key";

    private static final String PEM_FORMAT_EXTENSION = ".pem";

    private final String clientId;
    private final String clientSecret;
    private final UUID signingKeyId;
    private final UUID transportKeyId;
    private final X509Certificate clientSigningCertificate;
    private final X509Certificate clientTransportCertificate;
    private final String webApiKey;

    public static final Map<String, TypedAuthenticationMeans> typedAuthenticationMeans;

    static {
        Map<String, TypedAuthenticationMeans> typedClientConfigurationMap = new HashMap<>();
        typedClientConfigurationMap.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedClientConfigurationMap.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedClientConfigurationMap.put(CLIENT_SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedClientConfigurationMap.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedClientConfigurationMap.put(CLIENT_SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_SIGNING_CERTIFICATE_PEM);
        typedClientConfigurationMap.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedClientConfigurationMap.put(WEB_API_KEY_NAME, TypedAuthenticationMeans.API_KEY_STRING);
        typedAuthenticationMeans = unmodifiableMap(typedClientConfigurationMap);
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

    @SneakyThrows(CertificateException.class)
    public static BcrGroupAuthenticationMeans fromAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans, String providerKey) {
        if (!authenticationMeans.keySet().containsAll(typedAuthenticationMeans.keySet())) {
            Set<String> missingKeys = new HashSet<>(typedAuthenticationMeans.keySet());
            missingKeys.removeAll(authenticationMeans.keySet());
            throw new MissingAuthenticationMeansException(providerKey, missingKeys.toString());
        }

        String clientId = authenticationMeans.get(CLIENT_ID_NAME).getValue();
        String clientSecret = authenticationMeans.get(CLIENT_SECRET_NAME).getValue();
        X509Certificate clientTransportCertificate = KeyUtil.createCertificateFromPemFormat(authenticationMeans.get(CLIENT_TRANSPORT_CERTIFICATE_NAME).getValue());
        X509Certificate clientSigningCertificate = KeyUtil.createCertificateFromPemFormat(authenticationMeans.get(CLIENT_SIGNING_CERTIFICATE_NAME).getValue());
        UUID signingKid = UUID.fromString(authenticationMeans.get(CLIENT_SIGNING_KEY_ID_NAME).getValue());
        UUID transportKid = UUID.fromString(authenticationMeans.get(CLIENT_TRANSPORT_KEY_ID_NAME).getValue());
        String webApiKey = authenticationMeans.get(WEB_API_KEY_NAME).getValue();

        return new BcrGroupAuthenticationMeans(
                clientId,
                clientSecret,
                signingKid,
                transportKid,
                clientSigningCertificate,
                clientTransportCertificate,
                webApiKey);
    }
}

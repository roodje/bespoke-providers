package com.yolt.providers.gruppocedacri.common;

import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.RenderingType;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.common.domain.authenticationmeans.keymaterial.KeyRequirements;
import com.yolt.providers.common.domain.authenticationmeans.types.StringType;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.gruppocedacri.common.util.HsmUtils;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Builder
@Data
public class GruppoCedacriAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String EMAIL_NAME = "email";
    public static final String CANCEL_LINK_NAME = "cancel-link";
    public static final String CLIENT_TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String CLIENT_TRANSPORT_KEY_ID_NAME = "transport-key-id";

    @Getter
    private static final Map<String, TypedAuthenticationMeans> typedAuthenticationMeans;
    @Getter
    private static final Optional<KeyRequirements> transportKeyRequirements;

    static {
        typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(EMAIL_NAME, TypedAuthenticationMeans.CLIENT_EMAIL);
        typedAuthenticationMeans.put(CANCEL_LINK_NAME, new TypedAuthenticationMeans("Cancel link", StringType.getInstance(), RenderingType.ONE_LINE_STRING));
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CLIENT_TRANSPORT_CERTIFICATE_PEM);
        typedAuthenticationMeans.put(CLIENT_TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID_HEADER_STRING);

        transportKeyRequirements = HsmUtils.getKeyRequirements(CLIENT_TRANSPORT_KEY_ID_NAME, CLIENT_TRANSPORT_CERTIFICATE_NAME);
    }

    private final String clientId;
    private final String clientSecret;
    private final UUID transportKeyId;
    private final X509Certificate transportCertificate;

    public static GruppoCedacriAuthenticationMeans fromAuthenticationMeans(Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                           String provider) {
        GruppoCedacriAuthenticationMeansBuilder gruppoCedacriAuthenticationMeansBuilder =
                createMutualTlsAuthenticationMeans(typedAuthenticationMeans, provider);

        return gruppoCedacriAuthenticationMeansBuilder
                .clientId(typedAuthenticationMeans.get(CLIENT_ID_NAME).getValue())
                .clientSecret(typedAuthenticationMeans.get(CLIENT_SECRET_NAME).getValue())
                .build();
    }

    public static GruppoCedacriAuthenticationMeans fromAutoOnboardingAuthenticationMeans(Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                                         String provider) {
        return createMutualTlsAuthenticationMeans(typedAuthenticationMeans, provider).build();
    }

    private static GruppoCedacriAuthenticationMeansBuilder createMutualTlsAuthenticationMeans(Map<String, BasicAuthenticationMean> typedAuthenticationMeans,
                                                                                              String provider) {
        UUID keyId = UUID.fromString(typedAuthenticationMeans.get(CLIENT_TRANSPORT_KEY_ID_NAME).getValue());
        X509Certificate clientTransportCertificate = createCertificate(
                typedAuthenticationMeans.get(CLIENT_TRANSPORT_CERTIFICATE_NAME).getValue(), provider);

        return GruppoCedacriAuthenticationMeans.builder()
                .transportCertificate(clientTransportCertificate)
                .transportKeyId(keyId);
    }

    private static X509Certificate createCertificate(String certificateString,
                                                     String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, CLIENT_TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for PEM format");
        }
    }
}

package com.yolt.providers.redsys.common.auth;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.exception.InvalidAuthenticationMeansException;
import com.yolt.providers.common.util.KeyUtil;
import com.yolt.providers.redsys.common.model.SignatureData;
import lombok.Builder;
import lombok.Data;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.UUID;

@Builder
@Data
public class RedsysAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";
    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String SIGNING_KEY_ID_NAME = "signing-key-id";

    private final String clientId;
    private final UUID transportKeyId;
    private final X509Certificate transportCertificate;
    private final UUID signingKeyId;
    private final X509Certificate signingCertificate;

    public SignatureData getSigningData(Signer signer) {
        return SignatureData.builder()
                .signer(signer)
                .signingKeyId(signingKeyId)
                .signingCertificate(signingCertificate)
                .build();
    }

    public static RedsysAuthenticationMeans fromAuthenticationMeans(final Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                    final String provider) {

        return RedsysAuthenticationMeans.builder()
                .clientId(authenticationMeans.get(CLIENT_ID_NAME).getValue())
                .transportCertificate(createCertificate(authenticationMeans.get(TRANSPORT_CERTIFICATE_NAME).getValue(), provider))
                .transportKeyId(UUID.fromString(authenticationMeans.get(TRANSPORT_KEY_ID_NAME).getValue()))
                .signingCertificate(createCertificate(authenticationMeans.get(SIGNING_CERTIFICATE_NAME).getValue(), provider))
                .signingKeyId(UUID.fromString(authenticationMeans.get(SIGNING_KEY_ID_NAME).getValue()))
                .build();
    }

    private static X509Certificate createCertificate(final String certificateString,
                                                     final String provider) {
        try {
            return KeyUtil.createCertificateFromPemFormat(certificateString);
        } catch (CertificateException e) {
            throw new InvalidAuthenticationMeansException(provider, TRANSPORT_CERTIFICATE_NAME, "Cannot process certificate for thumbprint");
        }
    }
}

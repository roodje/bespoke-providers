package com.yolt.providers.triodosbank.common.auth;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.domain.authenticationmeans.BasicAuthenticationMean;
import com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans;
import com.yolt.providers.triodosbank.common.model.domain.SignatureData;
import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_ID_STRING;
import static com.yolt.providers.common.domain.authenticationmeans.TypedAuthenticationMeans.CLIENT_SECRET_STRING;

@Builder
@Data
public class TriodosBankAuthenticationMeans {
    public static final String TRANSPORT_CERTIFICATE_NAME = "transport-certificate";
    public static final String TRANSPORT_KEY_ID_NAME = "transport-key-id";
    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";
    public static final String SIGNING_KEY_ID_NAME = "signing-key-id";
    public static final String CLIENT_ID_STRING_NAME = "client-id";
    public static final String CLIENT_SECRET_STRING_NAME = "client-secret";

    private final X509Certificate transportCertificate;
    private final String transportKeyId;
    private final X509Certificate signingCertificate;
    private final String signingKeyId;
    private final String clientId;
    private final String clientSecret;

    public SignatureData getSignatureData(Signer signer) {
        return SignatureData.builder()
                .signer(signer)
                .signingKeyId(UUID.fromString(signingKeyId))
                .signingCertificate(signingCertificate)
                .build();
    }

    public static Map<String, TypedAuthenticationMeans> getTypedAuthenticationMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_STRING_NAME, CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_STRING_NAME, CLIENT_SECRET_STRING);
        typedAuthenticationMeans.put(SIGNING_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(SIGNING_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        typedAuthenticationMeans.put(TRANSPORT_KEY_ID_NAME, TypedAuthenticationMeans.KEY_ID);
        typedAuthenticationMeans.put(TRANSPORT_CERTIFICATE_NAME, TypedAuthenticationMeans.CERTIFICATE_PEM);
        return typedAuthenticationMeans;
    }

    public static Map<String, TypedAuthenticationMeans> getAutoConfiguredMeans() {
        Map<String, TypedAuthenticationMeans> typedAuthenticationMeans = new HashMap<>();
        typedAuthenticationMeans.put(CLIENT_ID_STRING_NAME, TypedAuthenticationMeans.CLIENT_ID_STRING);
        typedAuthenticationMeans.put(CLIENT_SECRET_STRING_NAME, TypedAuthenticationMeans.CLIENT_SECRET_STRING);
        return typedAuthenticationMeans;
    }

    public static TriodosBankAuthenticationMeans createAuthenticationMeans(Map<String, BasicAuthenticationMean> authenticationMeans,
                                                                           String providerIdentifier) {
        TriodosBankAuthenticationMeansAdapter adapter = new TriodosBankAuthenticationMeansAdapter(authenticationMeans, providerIdentifier);
        return TriodosBankAuthenticationMeans.builder()
                .transportCertificate(adapter.getCertificate(TRANSPORT_CERTIFICATE_NAME))
                .transportKeyId(adapter.getValue(TRANSPORT_KEY_ID_NAME))
                .signingCertificate(adapter.getCertificate(SIGNING_CERTIFICATE_NAME))
                .signingKeyId(adapter.getValue(SIGNING_KEY_ID_NAME))
                .clientId(adapter.getNullableValue(CLIENT_ID_STRING_NAME))
                .clientSecret(adapter.getNullableValue(CLIENT_SECRET_STRING_NAME))
                .build();
    }
}

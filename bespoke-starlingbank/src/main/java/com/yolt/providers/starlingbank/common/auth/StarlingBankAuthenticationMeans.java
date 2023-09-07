package com.yolt.providers.starlingbank.common.auth;

import lombok.Builder;
import lombok.Data;

import java.security.cert.X509Certificate;

@Data
@Builder
public class StarlingBankAuthenticationMeans {

    /**
     * Used for AIS & PIS
     */
    public static final String API_KEY_NAME_2 = "api-key-2";
    public static final String API_SECRET_NAME_2 = "api-secret-2";
    public static final String TRANSPORT_CERTIFICATE_NAME_2 = "transport-certificate-2";
    public static final String TRANSPORT_KEY_ID_NAME_2 = "transport-key-id-2";
    /**
     * Used only for PIS
     */
    public static final String SIGNING_KEY_HEADER_ID_NAME_2 = "private-signing-key-header-id-2";
    public static final String SIGNING_PRIVATE_KEY_ID_NAME_2 = "signing-private-key-id-2";

    private final String apiKey;
    private final String apiSecret;
    private final X509Certificate transportCertificate;
    private final String transportKeyId;
    private final String signingKeyHeaderId;
    private final String signingPrivateKeyId;
}

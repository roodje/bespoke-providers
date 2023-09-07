package com.yolt.providers.monorepogroup.qontogroup.common;

import lombok.Value;

import java.util.UUID;

@Value
public class QontoGroupAuthenticationMeans {

    public static final String CLIENT_ID_NAME = "client-id";
    public static final String CLIENT_SECRET_NAME = "client-secret";
    public static final String SIGNING_CERTIFICATE_ID_NAME = "signing-certificate-id";
    public static final String SIGNING_CERTIFICATE_NAME = "signing-certificate";

    private final SigningData signingData;
    private final String clientId;
    private final String clientSecret;

    public record SigningData(String certificateUrl, UUID signingKeyId) {
    }

}

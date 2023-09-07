package com.yolt.providers.stet.generic.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HttpHeadersExtension {

    public static final String DIGEST = "digest";
    public static final String SIGNATURE = "signature";
    public static final String X_REQUEST_ID = "x-request-id";
    public static final String PSU_IP_ADDRESS = "psu-ip-address";
}

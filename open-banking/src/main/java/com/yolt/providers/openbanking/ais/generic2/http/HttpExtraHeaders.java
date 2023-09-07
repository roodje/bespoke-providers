package com.yolt.providers.openbanking.ais.generic2.http;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpExtraHeaders {
    public static final String FINANCIAL_ID_HEADER_NAME = "x-fapi-financial-id";
    public static final String INTERACTION_ID_HEADER_NAME = "x-fapi-interaction-id";
    public static final String CUSTOMER_LAST_LOGGED_TIME_HEADER_NAME = "x-fapi-customer-last-logged-time";
    public static final String CUSTOMER_IP_ADDRESS_HEADER_NAME = "x-fapi-customer-ip-address";
    public static final String IDEMPOTENT_KEY = "x-idempotency-key";
    public static final String SIGNATURE_HEADER_NAME = "x-jws-signature";
}

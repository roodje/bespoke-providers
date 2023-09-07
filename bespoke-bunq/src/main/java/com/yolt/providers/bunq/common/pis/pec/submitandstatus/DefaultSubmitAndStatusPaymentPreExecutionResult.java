package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.yolt.providers.bunq.common.http.BunqPisHttpClient;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.security.KeyPair;

@RequiredArgsConstructor
@Data
public class DefaultSubmitAndStatusPaymentPreExecutionResult {

    private final BunqPisHttpClient httpClient;
    private final int paymentId;
    private final long psd2UserId;
    private final String sessionToken;
    private final long expirationTime;
    private final KeyPair keyPair;
}

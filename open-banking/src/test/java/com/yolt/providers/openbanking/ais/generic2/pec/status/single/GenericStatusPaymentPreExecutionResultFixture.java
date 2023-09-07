package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;

public class GenericStatusPaymentPreExecutionResultFixture {

    public static GenericPaymentStatusPreExecutionResult forUkDomesticStatus(DefaultAuthMeans authMeans, String accessToken, String paymentId, String consentId, RestTemplateManager restTemplateManager) {
        return new GenericPaymentStatusPreExecutionResult(accessToken,
                authMeans,
                restTemplateManager,
                paymentId,
                consentId);
    }
}

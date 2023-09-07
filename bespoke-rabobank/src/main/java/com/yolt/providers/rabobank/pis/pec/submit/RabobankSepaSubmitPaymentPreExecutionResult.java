package com.yolt.providers.rabobank.pis.pec.submit;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import lombok.Value;

@Value
public class RabobankSepaSubmitPaymentPreExecutionResult {

    private String paymentId;
    private RabobankAuthenticationMeans authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private String psuIpAddress;
    private Signer signer;
}

package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.rabobank.RabobankAuthenticationMeans;
import lombok.Value;

@Value
public class RabobankSepaInitiatePreExecutionResult {

    private RabobankAuthenticationMeans authenticationMeans;
    private SepaInitiatePaymentRequestDTO requestDTO;
    private String baseClientRedirectUrl;
    private Signer signer;
    private String psuIpAddress;
    private RestTemplateManager restTemplateManager;
    private String state;
}

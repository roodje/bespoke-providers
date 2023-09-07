package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import lombok.Value;

@Value
public class AbnAmroInitiatePaymentPreExecutionResult {

    private String accessToken;
    private AbnAmroAuthenticationMeans authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private SepaInitiatePaymentRequestDTO requestDTO;
    private String baseClientRedirectUrl;
    private String state;
}

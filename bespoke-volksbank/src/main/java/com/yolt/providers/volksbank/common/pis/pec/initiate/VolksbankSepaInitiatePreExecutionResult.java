package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VolksbankSepaInitiatePreExecutionResult {

    private SepaInitiatePaymentRequestDTO requestDTO;
    private VolksbankAuthenticationMeans authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private String psuIpAddress;
    private String state;
    private String baseClientRedirectUrl;
}

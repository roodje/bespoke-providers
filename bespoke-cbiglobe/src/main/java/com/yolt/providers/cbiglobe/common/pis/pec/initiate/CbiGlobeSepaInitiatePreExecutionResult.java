package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CbiGlobeSepaInitiatePreExecutionResult {

    private SepaInitiatePaymentRequestDTO requestDTO;
    private CbiGlobeAuthenticationMeans authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private String psuIpAddress;
    private String accessToken;
    private AspspData aspspData;
    private SignatureData signatureData;
    private String redirectUrlWithState;
}

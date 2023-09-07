package com.yolt.providers.abnamrogroup.common.pis.pec.submit;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.auth.AccessTokenResponseDTO;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import lombok.Value;

@Value
public class AbnAmroSubmitPaymentPreExecutionResult {

    private AccessTokenResponseDTO accessTokenResponseDTO;
    private AbnAmroAuthenticationMeans authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private String transactionId;
}

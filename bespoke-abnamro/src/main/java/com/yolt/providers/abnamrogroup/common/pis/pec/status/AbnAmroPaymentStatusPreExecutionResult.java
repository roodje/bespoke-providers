package com.yolt.providers.abnamrogroup.common.pis.pec.status;

import com.yolt.providers.abnamrogroup.common.auth.AbnAmroAuthenticationMeans;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroPaymentProviderState;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import lombok.Value;

@Value
public class AbnAmroPaymentStatusPreExecutionResult {

    private AbnAmroPaymentProviderState providerState;
    private AbnAmroAuthenticationMeans authenticationMeans;
    private RestTemplateManager restTemplateManager;
}

package com.yolt.providers.volksbank.common.pis.pec.submit;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.volksbank.common.auth.VolksbankAuthenticationMeans;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VolksbankSepaSubmitPreExecutionResult {

    private VolksbankAuthenticationMeans authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private String paymentId;
}

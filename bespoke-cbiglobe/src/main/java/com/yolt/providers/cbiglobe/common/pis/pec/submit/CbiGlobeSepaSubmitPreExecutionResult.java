package com.yolt.providers.cbiglobe.common.pis.pec.submit;

import com.yolt.providers.cbiglobe.common.auth.CbiGlobeAuthenticationMeans;
import com.yolt.providers.cbiglobe.common.config.AspspData;
import com.yolt.providers.cbiglobe.common.model.SignatureData;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CbiGlobeSepaSubmitPreExecutionResult {

    private CbiGlobeAuthenticationMeans authenticationMeans;
    private RestTemplateManager restTemplateManager;
    private String paymentId;
    private String accessToken;
    private AspspData aspspData;
    private SignatureData signatureData;
}

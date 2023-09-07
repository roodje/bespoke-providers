package com.yolt.providers.stet.generic.service.pec.authorization.token;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StetTokenPaymentPreExecutionResult {

    private Signer signer;
    private DefaultAuthenticationMeans authMeans;
    @Deprecated
    private RestTemplateManager restTemplateManager;
    private HttpClient httpClient;
    private String requestUrl;
    @Deprecated
    private String baseUrl;
}

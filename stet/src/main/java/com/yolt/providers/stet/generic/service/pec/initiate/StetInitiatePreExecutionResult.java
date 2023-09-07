package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
@Builder
public class StetInitiatePreExecutionResult {

    private Signer signer;
    private DefaultAuthenticationMeans authMeans;
    @Deprecated
    private RestTemplateManager restTemplateManager;
    private HttpClient httpClient;
    private String state;
    private String baseClientRedirectUrl;
    private HttpMethod httpMethod;
    private String requestPath;
    private String accessToken;
    private String psuIpAddress;
    private SepaInitiatePaymentRequestDTO sepaRequestDTO;
}

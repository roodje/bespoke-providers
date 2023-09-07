package com.yolt.providers.stet.generic.service.pec.confirmation;

import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
@Builder
public class StetConfirmationPreExecutionResult {

    private Signer signer;
    private DefaultAuthenticationMeans authMeans;
    @Deprecated
    private RestTemplateManager restTemplateManager;
    private HttpClient httpClient;
    private String redirectUrlPostedBackFromSite;
    private HttpMethod httpMethod;
    private String requestPath;
    private String paymentId;
    private String accessToken;
    private String psuIpAddress;
}

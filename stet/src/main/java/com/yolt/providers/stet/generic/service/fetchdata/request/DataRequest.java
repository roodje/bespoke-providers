package com.yolt.providers.stet.generic.service.fetchdata.request;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DataRequest {

    private final String baseUrl;
    private final Signer signer;
    private final DefaultAuthenticationMeans authMeans;
    private final String accessToken;
    private final String psuIpAddress;
    private final boolean isRefreshedToken;

    public DataRequest(FetchDataRequest fetchDataRequest) {
        DataProviderState providerState = fetchDataRequest.getProviderState();
        this.baseUrl = providerState.getRegion().getBaseUrl();
        this.signer = fetchDataRequest.getSigner();
        this.authMeans = fetchDataRequest.getAuthMeans();
        this.accessToken = providerState.getAccessToken();
        this.psuIpAddress = fetchDataRequest.getPsuIpAddress();
        this.isRefreshedToken = providerState.isRefreshed();
    }
}

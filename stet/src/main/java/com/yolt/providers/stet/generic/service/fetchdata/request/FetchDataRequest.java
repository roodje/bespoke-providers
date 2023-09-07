package com.yolt.providers.stet.generic.service.fetchdata.request;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.stet.generic.auth.DefaultAuthenticationMeans;
import com.yolt.providers.stet.generic.domain.DataProviderState;
import lombok.Value;

import java.time.Instant;

@Value
public class FetchDataRequest {

    private DataProviderState providerState;
    private Instant transactionsFetchStartTime;
    private Signer signer;
    private DefaultAuthenticationMeans authMeans;
    private String psuIpAddress;
}

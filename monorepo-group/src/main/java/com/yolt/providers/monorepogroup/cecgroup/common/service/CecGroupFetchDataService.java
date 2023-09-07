package com.yolt.providers.monorepogroup.cecgroup.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.monorepogroup.cecgroup.common.CecGroupAccessMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.auth.CecGroupAuthenticationMeans;
import com.yolt.providers.monorepogroup.cecgroup.common.http.CecGroupHttpClient;

import java.time.Instant;

public interface CecGroupFetchDataService {

    DataProviderResponse fetchData(CecGroupAuthenticationMeans authMeans,
                                   CecGroupAccessMeans fromJson,
                                   CecGroupHttpClient httpClient,
                                   Signer signer,
                                   String psuIpAddress,
                                   Instant transactionsFetchStartTime,
                                   String providerDisplayName) throws ProviderFetchDataException;
}

package com.yolt.providers.monorepogroup.olbgroup.common.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.olbgroup.common.auth.OlbGroupAuthenticationMeans;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Instant;

public interface OlbGroupFetchDataService {
    DataProviderResponse fetchAccountsAndTransactions(OlbGroupAuthenticationMeans authMeans,
                                                      AccessMeansDTO accessMeans,
                                                      RestTemplateManager restTemplateManager,
                                                      String psuIpAddress,
                                                      Instant transactionsFetchStartTime) throws ProviderFetchDataException, TokenInvalidException;
}

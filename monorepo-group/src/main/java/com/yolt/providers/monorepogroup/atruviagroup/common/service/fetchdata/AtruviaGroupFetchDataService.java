package com.yolt.providers.monorepogroup.atruviagroup.common.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.monorepogroup.atruviagroup.common.authenticationmeans.AtruviaGroupAuthenticationMeans;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Instant;

public interface AtruviaGroupFetchDataService {
    DataProviderResponse fetchAccountsAndTransactions(AtruviaGroupAuthenticationMeans authMeans,
                                                      AccessMeansDTO accessMeans,
                                                      RestTemplateManager restTemplateManager,
                                                      String psuIpAddress,
                                                      Instant transactionsFetchStartTime,
                                                      Signer signer) throws ProviderFetchDataException, TokenInvalidException;
}

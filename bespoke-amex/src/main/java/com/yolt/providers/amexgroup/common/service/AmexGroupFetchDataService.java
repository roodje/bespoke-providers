package com.yolt.providers.amexgroup.common.service;

import com.yolt.providers.amexgroup.common.auth.AmexGroupAuthMeans;
import com.yolt.providers.amexgroup.common.dto.TokenResponses;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;

import java.time.Instant;

public interface AmexGroupFetchDataService {
    DataProviderResponse getAccountsAndTransactions(AmexGroupAuthMeans amexGroupAuthMeans,
                                                    RestTemplateManager restTemplateManager,
                                                    TokenResponses tokenResponses,
                                                    Instant requestedTransactionsFetchStartTime) throws ProviderFetchDataException, TokenInvalidException;
}

package com.yolt.providers.starlingbank.common.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.starlingbank.common.http.StarlingBankHttpClient;
import nl.ing.lovebird.providershared.AccessMeansDTO;

import java.time.Instant;

public interface StarlingBankFetchDataService {

    DataProviderResponse getAccountsAndTransactions(StarlingBankHttpClient httpClient, AccessMeansDTO accessMeansDTO, Instant fetchStartTime) throws ProviderFetchDataException, TokenInvalidException;

}

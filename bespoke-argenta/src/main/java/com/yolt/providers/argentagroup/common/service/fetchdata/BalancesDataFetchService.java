package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.util.Map;

public interface BalancesDataFetchService {

    public Map<BalanceType, BalanceDTO> getBalances(final UrlFetchDataRequest request,
                                                    final DefaultAuthenticationMeans authenticationMeans,
                                                    final HttpClient httpClient,
                                                    final ProviderAccountDTO account,
                                                    final AccessMeans accessMeans) throws TokenInvalidException;
}

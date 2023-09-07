package com.yolt.providers.stet.generic.service.fetchdata.balance;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;

import java.util.List;

public interface FetchBalancesStrategy {

    List<StetBalanceDTO> fetchBalances(HttpClient httpClient,
                                       String endpoint,
                                       DataRequest dataRequest,
                                       StetAccountDTO account) throws TokenInvalidException;
}

package com.yolt.providers.stet.societegeneralegroup.common.service.fetchdata.balance;

import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class SocieteGeneraleGroupFetchBasicBalancesStrategy implements FetchBalancesStrategy {

    @Override
    public List<StetBalanceDTO> fetchBalances(HttpClient httpClient,
                                              String endpoint,
                                              DataRequest dataRequest,
                                              StetAccountDTO account) {
        return Collections.emptyList();
    }
}

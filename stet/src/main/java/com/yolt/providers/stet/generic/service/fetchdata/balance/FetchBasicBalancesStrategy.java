package com.yolt.providers.stet.generic.service.fetchdata.balance;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalancesResponseDTO;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class FetchBasicBalancesStrategy implements FetchBalancesStrategy {

    private final FetchDataRestClient restClient;

    @Override
    public List<StetBalanceDTO> fetchBalances(HttpClient httpClient,
                                              String endpoint,
                                              DataRequest dataRequest,
                                              StetAccountDTO account) throws TokenInvalidException {
        StetBalancesResponseDTO halBalances = restClient.getBalances(httpClient, endpoint, dataRequest);
        if (Objects.isNull(halBalances) || CollectionUtils.isEmpty(halBalances.getBalances())) {
            return Collections.emptyList();
        }
        return new ArrayList<>(halBalances.getBalances());
    }
}

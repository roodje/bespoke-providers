package com.yolt.providers.stet.lclgroup.common.fetchdata;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBasicBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;

import java.util.List;
import java.util.stream.Collectors;

import static com.yolt.providers.stet.generic.dto.balance.StetBalanceType.OTHR;

public class LclFetchBalanceStrategy extends FetchBasicBalancesStrategy {

    public LclFetchBalanceStrategy(final FetchDataRestClient restClient) {
        super(restClient);
    }

    @Override
    public List<StetBalanceDTO> fetchBalances(final HttpClient httpClient, final String endpoint, final DataRequest dataRequest, final StetAccountDTO account) throws TokenInvalidException {
        return super.fetchBalances(httpClient, endpoint, dataRequest, account)
                .stream()
                .filter(balance -> (balance.getType() != null) && (balance.getType() != OTHR))
                .collect(Collectors.toList());

    }
}

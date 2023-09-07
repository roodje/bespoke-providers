package com.yolt.providers.stet.generic.service.fetchdata.balance;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class FetchExtractedBalancesStrategy extends FetchBasicBalancesStrategy {

    public FetchExtractedBalancesStrategy(FetchDataRestClient restClient) {
        super(restClient);
    }

    @Override
    public List<StetBalanceDTO> fetchBalances(HttpClient httpClient,
                                              String balancesEndpoint,
                                              DataRequest dataRequest,
                                              StetAccountDTO account) throws TokenInvalidException {
        List<StetBalanceDTO> balances = account.getBalances();
        if (!CollectionUtils.isEmpty(balances)) {
            return balances;
        }
        return super.fetchBalances(httpClient, balancesEndpoint, dataRequest, account);
    }
}

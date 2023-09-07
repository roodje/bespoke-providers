package com.yolt.providers.stet.generic.service.fetchdata.account;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountsResponseDTO;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import lombok.RequiredArgsConstructor;
import org.springframework.util.CollectionUtils;

import java.util.*;

@RequiredArgsConstructor
public class FetchBasicAccountsStrategy implements FetchAccountsStrategy {

    protected final FetchDataRestClient restClient;

    @Override
    public List<StetAccountDTO> fetchAccounts(HttpClient httpClient,
                                              String accountsEndpoint,
                                              String consentsEndpoint,
                                              DataRequest dataRequest) throws TokenInvalidException {
        StetAccountsResponseDTO accountsResponseDTO = restClient.getAccounts(httpClient, accountsEndpoint, dataRequest);
        if (Objects.isNull(accountsResponseDTO) || CollectionUtils.isEmpty(accountsResponseDTO.getAccounts())) {
            return Collections.emptyList();
        }
        return new ArrayList<>(accountsResponseDTO.getAccounts());
    }
}

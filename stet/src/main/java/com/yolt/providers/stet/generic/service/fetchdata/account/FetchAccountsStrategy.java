package com.yolt.providers.stet.generic.service.fetchdata.account;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;

import java.util.List;

public interface FetchAccountsStrategy {

    List<StetAccountDTO> fetchAccounts(HttpClient httpClient,
                                       String accountsEndpoint,
                                       String consentsEndpoint,
                                       DataRequest dataRequest) throws TokenInvalidException;
}

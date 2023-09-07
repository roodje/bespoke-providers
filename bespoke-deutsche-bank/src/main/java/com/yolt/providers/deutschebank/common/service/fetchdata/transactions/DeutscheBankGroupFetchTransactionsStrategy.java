package com.yolt.providers.deutschebank.common.service.fetchdata.transactions;

import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClient;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.util.List;

public interface DeutscheBankGroupFetchTransactionsStrategy {

    List<ProviderTransactionDTO> fetchTransactions(DeutscheBankGroupHttpClient httpClient,
                                                   UrlFetchDataRequest request,
                                                   DeutscheBankGroupProviderState providerState,
                                                   String accountId,
                                                   String accountName) throws TokenInvalidException;
}

package com.yolt.providers.deutschebank.common.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.deutschebank.common.domain.DeutscheBankGroupProviderState;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.Account;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.Balance;
import com.yolt.providers.deutschebank.common.domain.model.fetchdata.BalancesResponse;
import com.yolt.providers.deutschebank.common.http.DeutscheBankGroupHttpClient;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupAccountMapper;
import com.yolt.providers.deutschebank.common.mapper.DeutscheBankGroupProviderStateMapper;
import com.yolt.providers.deutschebank.common.service.fetchdata.accounts.DeutscheBankGroupFetchAccountsStrategy;
import com.yolt.providers.deutschebank.common.service.fetchdata.transactions.DeutscheBankGroupFetchTransactionsStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class DeutscheBankGroupFetchDataService {

    private static final String BALANCES_TEMPLATE = "/v1/accounts/{accountId}/balances";

    private final DeutscheBankGroupFetchAccountsStrategy fetchAccountsStrategy;
    private final DeutscheBankGroupAccountMapper accountMapper;
    private final DeutscheBankGroupProviderStateMapper providerStateMapper;
    private final DeutscheBankGroupFetchTransactionsStrategy fetchTransactionsStrategy;

    public DataProviderResponse fetchAccountsAndTransactions(String providerDisplayName,
                                                             DeutscheBankGroupHttpClient httpClient,
                                                             UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> providerAccountsDTO = new ArrayList<>();

        DeutscheBankGroupProviderState providerState = providerStateMapper.fromAccessMeansDTO(request.getAccessMeans());

        for (Account account : fetchAccountsStrategy.fetchAccounts(httpClient, request, providerState)) {
            String accountId = account.getResourceId();
            try {
                List<Balance> balances = account.getBalances();
                if (CollectionUtils.isEmpty(balances)) {
                    balances = fetchBalances(httpClient, request, providerState, accountId);
                }
                ProviderAccountDTO providerAccountDTO = accountMapper.mapProviderAccountDTO(providerDisplayName, account, balances);
                List<ProviderTransactionDTO> transaction = fetchTransactionsStrategy.fetchTransactions(httpClient, request, providerState, accountId, providerAccountDTO.getName());
                providerAccountDTO.getTransactions().addAll(transaction);
                providerAccountsDTO.add(providerAccountDTO);
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(providerAccountsDTO);
    }

    private List<Balance> fetchBalances(DeutscheBankGroupHttpClient httpClient,
                                        UrlFetchDataRequest request,
                                        DeutscheBankGroupProviderState providerState,
                                        String accountId) throws TokenInvalidException {
        String url = UriComponentsBuilder.fromUriString(BALANCES_TEMPLATE)
                .buildAndExpand(accountId)
                .toUriString();

        BalancesResponse response = httpClient.getBalances(url, providerState, request.getPsuIpAddress());
        return response.getBalances();
    }
}

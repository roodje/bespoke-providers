package com.yolt.providers.stet.generic.service.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import com.yolt.providers.stet.generic.config.DefaultProperties;
import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.transaction.StetTransactionDTO;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.AccountMapper;
import com.yolt.providers.stet.generic.mapper.providerstate.ProviderStateMapper;
import com.yolt.providers.stet.generic.mapper.transaction.TransactionMapper;
import com.yolt.providers.stet.generic.service.fetchdata.account.FetchAccountsStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.balance.FetchBalancesStrategy;
import com.yolt.providers.stet.generic.service.fetchdata.request.DataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.request.FetchDataRequest;
import com.yolt.providers.stet.generic.service.fetchdata.rest.FetchDataRestClient;
import com.yolt.providers.stet.generic.service.fetchdata.transaction.FetchTransactionsStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class DefaultFetchDataService implements FetchDataService {

    protected final FetchDataRestClient restClient;
    protected final ProviderStateMapper providerStateMapper;
    protected final FetchAccountsStrategy fetchAccountsStrategy;
    protected final FetchTransactionsStrategy fetchTransactionsStrategy;
    protected final FetchBalancesStrategy fetchBalancesStrategy;
    protected final DateTimeSupplier dateMapper;
    protected final AccountMapper accountMapper;
    protected final TransactionMapper transactionMapper;
    protected final DefaultProperties properties;

    protected static final String CONSENTS_ENDPOINT = "/consents";
    protected static final String ACCOUNTS_ENDPOINT = "/accounts";
    protected static final String BALANCES_TEMPLATE = "/accounts/{accountId}/balances";
    protected static final String TRANSACTIONS_TEMPLATE = "/accounts/{accountId}/transactions";

    @Override
    public DataProviderResponse getAccountsAndTransactions(HttpClient httpClient,
                                                           FetchDataRequest fetchDataRequest) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

        String accountId = null;
        DataRequest dataRequest = new DataRequest(fetchDataRequest);
        try {
            List<StetAccountDTO> accounts = fetchAccountsStrategy.fetchAccounts(
                    httpClient,
                    getAccountsEndpoint(),
                    getConsentsEndpoint(),
                    dataRequest);

            for (StetAccountDTO account : accounts) {
                if (shouldIgnoreProcessingAccount(account)) {
                    continue;
                }
                accountId = account.getResourceId();

                List<StetBalanceDTO> balances = fetchBalancesStrategy.fetchBalances(
                        httpClient,
                        resolveUrlTemplate(getBalancesTemplate(), accountId),
                        dataRequest,
                        account);

                List<StetTransactionDTO> transactions = fetchTransactionsStrategy.fetchTransactions(
                        httpClient,
                        resolveUrlTemplate(getTransactionsTemplate(), accountId),
                        dataRequest,
                        fetchDataRequest.getTransactionsFetchStartTime());

                List<ProviderTransactionDTO> transactionDTOs = transactionMapper.mapToProviderTransactionDTOs(transactions);
                ProviderAccountDTO accountDTO = accountMapper.mapToProviderAccountDTO(account, balances, transactionDTOs);
                responseAccounts.add(accountDTO);
            }
        } catch (ProviderHttpStatusException | HttpStatusCodeException e) {
            throw new ProviderFetchDataException(e);
        }
        return new DataProviderResponse(Collections.unmodifiableList(responseAccounts));
    }

    /**
     * Omits account processing for various business reasons.
     * By default, it doesn't skip any account.
     */
    protected boolean shouldIgnoreProcessingAccount(StetAccountDTO account) { //NOSONAR It is provided to customize logic for unsupported accounts
        return false;
    }

    protected String resolveUrlTemplate(String urlTemplate, Object... uriVariables) {
        return UriComponentsBuilder.fromUriString(urlTemplate)
                .buildAndExpand(uriVariables)
                .toUriString();
    }

    protected String getTransactionsTemplate() {
        return TRANSACTIONS_TEMPLATE;
    }

    protected String getBalancesTemplate() {
        return BALANCES_TEMPLATE;
    }

    protected String getConsentsEndpoint() {
        return CONSENTS_ENDPOINT;
    }

    protected String getAccountsEndpoint() {
        return ACCOUNTS_ENDPOINT;
    }
}

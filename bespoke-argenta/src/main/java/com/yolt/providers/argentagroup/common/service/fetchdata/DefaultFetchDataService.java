package com.yolt.providers.argentagroup.common.service.fetchdata;

import com.yolt.providers.argentagroup.common.service.DefaultAuthenticationMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeans;
import com.yolt.providers.argentagroup.common.service.token.AccessMeansMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.http.HttpClient;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class DefaultFetchDataService implements FetchDataService {

    private final BalanceType availableBalanceType;
    private final BalanceType currentBalanceType;
    private final AccessMeansMapper accessMeansMapper;
    private final AccountsDataFetchService accountsDataFetchService;
    private final BalancesDataFetchService balancesDataFetchService;
    private final TransactionsDataFetchService transactionsDataFetchService;

    @Override
    public DataProviderResponse fetchData(final UrlFetchDataRequest request,
                                          final DefaultAuthenticationMeans authenticationMeans,
                                          final HttpClient httpClient) throws TokenInvalidException, ProviderFetchDataException {
        AccessMeans accessMeans = accessMeansMapper.deserializeAccessMeans(request.getAccessMeans().getAccessMeans());

        List<ProviderAccountDTO> accounts = accountsDataFetchService.getAccounts(
                request, authenticationMeans, httpClient, accessMeans);


        List<ProviderAccountDTO> enrichedAccounts = new ArrayList<>();
        for (ProviderAccountDTO account : accounts) {
            try {
                Map<BalanceType, BalanceDTO> balancesMap = balancesDataFetchService.getBalances(
                        request, authenticationMeans, httpClient, account, accessMeans);

                List<ProviderTransactionDTO> transactions = transactionsDataFetchService.getTransactions(
                        request, authenticationMeans, httpClient, account, accessMeans);

                ProviderAccountDTO enrichedAccount = enrichAccount(account, balancesMap, transactions);

                enrichedAccounts.add(enrichedAccount);
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(enrichedAccounts);
    }

    private ProviderAccountDTO enrichAccount(final ProviderAccountDTO account,
                                             final Map<BalanceType, BalanceDTO> balancesMap,
                                             final List<ProviderTransactionDTO> transactions) {
        BigDecimal availableBalance = Optional.of(balancesMap)
                .map(m -> m.get(availableBalanceType))
                .map(BalanceDTO::getBalanceAmount)
                .map(BalanceAmountDTO::getAmount)
                .orElseThrow(() -> new MissingDataException("Balance of type: " + availableBalanceType.getName() + " not found"));

        BigDecimal currentBalance = Optional.of(balancesMap)
                .map(m -> m.get(currentBalanceType))
                .map(BalanceDTO::getBalanceAmount)
                .map(BalanceAmountDTO::getAmount)
                .orElseThrow(() -> new MissingDataException("Balance of type: " + currentBalanceType.getName() + " not found"));

        return account.toBuilder()
                .availableBalance(availableBalance)
                .currentBalance(currentBalance)
                .transactions(transactions)
                .extendedAccount(account.getExtendedAccount()
                        .toBuilder()
                        .balances(List.copyOf(balancesMap.values()))
                        .build()
                )
                .build();
    }
}

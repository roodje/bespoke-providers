package com.yolt.providers.fabric.common.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.fabric.common.http.FabricDefaultHttpClient;
import com.yolt.providers.fabric.common.mapper.AccountMapper;
import com.yolt.providers.fabric.common.model.Account;
import com.yolt.providers.fabric.common.model.Balances;
import com.yolt.providers.fabric.common.model.GroupProviderState;
import com.yolt.providers.fabric.common.model.Transactions;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@AllArgsConstructor
public class DefaultFetchDataService {
    private final AccountMapper accountMapper;

    public DataProviderResponse fetchData(final GroupProviderState providerState,
                                          final FabricDefaultHttpClient httpClient,
                                          final String psuIpAddress,
                                          final Instant fetchDataStartTime) throws TokenInvalidException, ProviderFetchDataException {

        List<ProviderAccountDTO> providerAccountsDTO = new ArrayList<>();

        for (Account account : httpClient.getAccounts(providerState.getConsentId(), psuIpAddress).getAccounts()) {
            String accountId = account.getResourceId();
            try {
                Balances balances = fetchBalances(
                        httpClient,
                        providerState.getConsentId(),
                        accountId,
                        psuIpAddress);

                List<Transactions> transactions = getAllTransactions(
                        httpClient,
                        accountId,
                        fetchDataStartTime,
                        providerState.getConsentId(),
                        psuIpAddress);

                ProviderAccountDTO providerAccountDTO = accountMapper.map(account, balances.getBalances(), transactions);
                providerAccountsDTO.add(providerAccountDTO);
            } catch (Exception e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(providerAccountsDTO);
    }

    private Balances fetchBalances(final FabricDefaultHttpClient httpClient, final String consentId, final String accountId, final String psuIpAddress) throws TokenInvalidException {
        return httpClient.getBalances(consentId, accountId, psuIpAddress);
    }

    private List<Transactions> getAllTransactions(final FabricDefaultHttpClient httpClient, final String accountId, final Instant fetchDataStartTime,
                                                  final String consentId, final String psuIpAddress) throws TokenInvalidException {
        List<Transactions> transactionsList = new ArrayList<>();
        transactionsList.add(httpClient.getTransactions(accountId, fetchDataStartTime, consentId, psuIpAddress));
        return transactionsList;
    }
}


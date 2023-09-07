package com.yolt.providers.bancatransilvania.common.service;

import com.yolt.providers.bancatransilvania.common.config.BancaTransilvaniaGroupProperties;
import com.yolt.providers.bancatransilvania.common.domain.BancaTransilvaniaGroupProviderState;
import com.yolt.providers.bancatransilvania.common.domain.model.fetchdata.Account;
import com.yolt.providers.bancatransilvania.common.domain.model.fetchdata.AccountsResponse;
import com.yolt.providers.bancatransilvania.common.domain.model.fetchdata.Balance;
import com.yolt.providers.bancatransilvania.common.domain.model.fetchdata.TransactionsResponse;
import com.yolt.providers.bancatransilvania.common.http.BancaTransilvaniaGroupHttpClient;
import com.yolt.providers.bancatransilvania.common.mapper.BancaTransilvaniaGroupAccountMapper;
import com.yolt.providers.bancatransilvania.common.mapper.BancaTransilvaniaGroupProviderStateMapper;
import com.yolt.providers.bancatransilvania.common.mapper.BancaTransilvaniaGroupTransactionMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static nl.ing.lovebird.extendeddata.transaction.TransactionStatus.BOOKED;

@Slf4j
@RequiredArgsConstructor
public class BancaTransilvaniaGroupFetchDataService {

    private final BancaTransilvaniaGroupAccountMapper accountMapper;
    private final BancaTransilvaniaGroupTransactionMapper transactionMapper;
    private final BancaTransilvaniaGroupProviderStateMapper providerStateMapper;
    private final BancaTransilvaniaGroupProperties properties;
    private final Clock clock;

    public DataProviderResponse fetchData(BancaTransilvaniaGroupHttpClient httpClient,
                                          UrlFetchDataRequest request) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> providerAccountsDTO = new ArrayList<>();

        BancaTransilvaniaGroupProviderState providerState = providerStateMapper.fromAccessMeansDTO(request.getAccessMeans());

        for (Account account : fetchAccounts(httpClient, request, providerState)) {
            String accountId = account.getResourceId();
            try {
                List<Balance> balances = account.getBalances();
                List<ProviderTransactionDTO> transaction = fetchTransactions(httpClient, request, providerState, accountId);

                ProviderAccountDTO providerAccountDTO = accountMapper.mapProviderAccountDTO(account, balances, transaction);
                providerAccountsDTO.add(providerAccountDTO);
            } catch (ProviderHttpStatusException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(providerAccountsDTO);
    }

    private List<Account> fetchAccounts(BancaTransilvaniaGroupHttpClient httpClient,
                                        UrlFetchDataRequest request,
                                        BancaTransilvaniaGroupProviderState providerState) throws TokenInvalidException, ProviderFetchDataException {
        try {
            AccountsResponse accountsResponse = httpClient.getAccounts(providerState, request.getPsuIpAddress());
            return accountsResponse.getAccounts();
        } catch (ProviderHttpStatusException e) {
            throw new ProviderFetchDataException(e);
        }
    }

    private List<ProviderTransactionDTO> fetchTransactions(BancaTransilvaniaGroupHttpClient httpClient,
                                                           UrlFetchDataRequest request,
                                                           BancaTransilvaniaGroupProviderState providerState,
                                                           String accountId) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactionsDTO = new ArrayList<>();

        String url = UriComponentsBuilder.fromUriString(httpClient.getTransactionsTemplatePath())
                .queryParam("bookingStatus", "booked")
                .queryParam("dateFrom", convertToNarrowedDateFormat(clock, request.getTransactionsFetchStartTime(), 89))
                .buildAndExpand(accountId)
                .toUriString();

        int pageCounter = 1;
        while (StringUtils.isNotEmpty(url) && (pageCounter <= properties.getPaginationLimit())) {
            TransactionsResponse response = httpClient.getTransactions(url, providerState, request.getPsuIpAddress());
            transactionsDTO.addAll(transactionMapper.mapProviderTransactionsDTO(response.getBookedTransactions(), BOOKED));

            url = response.getNextHref();
            pageCounter++;
        }
        return transactionsDTO;
    }

    public static String convertToNarrowedDateFormat(Clock clock, Instant instant, int pastDays) {
        Instant dateBoundary = Instant.now(clock).minus(Period.ofDays(pastDays));
        if (instant.isBefore(dateBoundary)) {
            return formatToDate(dateBoundary);
        }
        return formatToDate(instant);
    }

    private static String formatToDate(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd")
                .withZone(ZoneId.of("Europe/Bucharest"))
                .format(instant);
    }
}

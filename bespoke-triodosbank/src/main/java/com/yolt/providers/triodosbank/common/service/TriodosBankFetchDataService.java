package com.yolt.providers.triodosbank.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.triodosbank.common.config.TriodosBankBaseProperties;
import com.yolt.providers.triodosbank.common.mapper.TriodosBankAccountMapper;
import com.yolt.providers.triodosbank.common.mapper.TriodosBankTransactionMapper;
import com.yolt.providers.triodosbank.common.model.Account;
import com.yolt.providers.triodosbank.common.model.Balance;
import com.yolt.providers.triodosbank.common.model.TransactionLinks;
import com.yolt.providers.triodosbank.common.model.Transactions;
import com.yolt.providers.triodosbank.common.model.domain.TriodosBankProviderState;
import com.yolt.providers.triodosbank.common.rest.TriodosBankHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.time.ZoneOffset.UTC;

@Slf4j
@RequiredArgsConstructor
public class TriodosBankFetchDataService {

    private final TriodosBankAccountMapper accountMapper;
    private final TriodosBankTransactionMapper transactionMapper;
    private final TriodosBankBaseProperties properties;

    public DataProviderResponse fetchData(TriodosBankHttpClient httpClient,
                                          Instant fetchStartTime,
                                          TriodosBankProviderState providerState) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> providerAccountsDTO = new ArrayList<>();
        String consentId = providerState.getConsentId();
        String accessToken = providerState.getAccessToken();

        List<Account> accounts = fetchAccounts(httpClient, consentId, accessToken);
        for (Account account : accounts) {
            try {
                List<Balance> balances = fetchBalances(httpClient, consentId, accessToken, account);
                List<ProviderTransactionDTO> transaction = fetchAllTransactions(httpClient, consentId, accessToken, fetchStartTime, account);

                ProviderAccountDTO providerAccountDTO = accountMapper.mapProviderAccountDTO(account, balances, transaction);
                providerAccountsDTO.add(providerAccountDTO);
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }
        return new DataProviderResponse(providerAccountsDTO);
    }

    private List<Account> fetchAccounts(TriodosBankHttpClient httpClient, String consentId, String token) throws TokenInvalidException {
        return httpClient.getAccounts(consentId, token).getAccounts();
    }

    private List<Balance> fetchBalances(TriodosBankHttpClient httpClient, String consentId, String token, Account account) throws TokenInvalidException {
        return httpClient.getBalances(consentId, token, account.getBalancesUrl()).getBalances();
    }

    private List<ProviderTransactionDTO> fetchAllTransactions(TriodosBankHttpClient httpClient,
                                                              String consentId,
                                                              String accessToken,
                                                              Instant fetchStartTime,
                                                              Account account) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactions = new ArrayList<>();

        int pageCounter = 0;
        String previousPageUrl;
        String currentPageUrl = assembleCurrentPageUrl(account.getTransactionsUrl(), fetchStartTime);

        do {
            TransactionLinks links = fetchTransactions(transactions, httpClient, consentId, accessToken, currentPageUrl);
            previousPageUrl = currentPageUrl;
            currentPageUrl = getNextPageAndFormatItProperly(links);
            pageCounter++;
        } while (shouldContinueFetchingTransaction(currentPageUrl, previousPageUrl, pageCounter));
        return transactions;
    }

    /**
     * The next page link contains a edgeToken parameter, that ends with %3D char
     * we need to format it properly to "=", before using it
     */
    private String getNextPageAndFormatItProperly(TransactionLinks links) {
        try {
            if (StringUtils.isNotEmpty(links.getNext())) {
                return URLDecoder.decode(links.getNext(), "UTF-8");
            }
            return StringUtils.EMPTY;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("The link from the next transaction could not be parsed properly");
        }
    }

    private String assembleCurrentPageUrl(String transactionsUrl, Instant fetchStartTime) {
        LocalDate startTime = LocalDateTime.ofInstant(fetchStartTime, UTC).toLocalDate();
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(transactionsUrl)
                .queryParam("bookingStatus", "both")
                .queryParam("dateFrom", startTime);

        return uriBuilder.toUriString();
    }

    private boolean shouldContinueFetchingTransaction(String currentPageUrl, String previousPageUrl, int pageCounter) {
        return StringUtils.isNotEmpty(currentPageUrl) &&
                !Objects.equals(currentPageUrl, previousPageUrl) &&
                (pageCounter < properties.getTransactionsPaginationLimit());
    }

    private TransactionLinks fetchTransactions(List<ProviderTransactionDTO> transactions,
                                               TriodosBankHttpClient httpClient,
                                               String consentId,
                                               String accessToken,
                                               String transactionsUrl) throws TokenInvalidException {
        Transactions halTransactions = httpClient
                .getTransactions(consentId, accessToken, transactionsUrl)
                .getTransactions();

        transactions.addAll(transactionMapper.mapProviderTransactionsDTO(halTransactions));
        return halTransactions.getLinks();
    }
}

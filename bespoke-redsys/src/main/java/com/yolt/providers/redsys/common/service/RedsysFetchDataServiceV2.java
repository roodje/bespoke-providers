package com.yolt.providers.redsys.common.service;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.cryptography.RestTemplateManager;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.common.rest.tracing.ExternalTracingUtil;
import com.yolt.providers.redsys.common.auth.RedsysAuthenticationMeans;
import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import com.yolt.providers.redsys.common.dto.*;
import com.yolt.providers.redsys.common.model.RedsysAccessMeans;
import com.yolt.providers.redsys.common.model.SignatureData;
import com.yolt.providers.redsys.common.rest.BookingStatus;
import com.yolt.providers.redsys.common.rest.RedsysHttpClient;
import com.yolt.providers.redsys.common.rest.RedsysRestTemplateService;
import com.yolt.providers.redsys.common.service.mapper.RedsysDataMapperService;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.transaction.TransactionStatus;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

import static com.yolt.providers.redsys.common.util.ErrorHandlerUtil.handleNon2xxResponseInFetchData;
import static nl.ing.lovebird.providerdomain.ProviderAccountNumberDTO.Scheme.IBAN;

@RequiredArgsConstructor
@Deprecated //There is newer version
public class RedsysFetchDataServiceV2 {

    private final RedsysRestTemplateService restTemplateService;
    private final RedsysBaseProperties properties;
    private final RedsysDataMapperService mapperService;
    private final BookingStatus bookingStatus;
    private final TransactionsFetchStartTime transactionsFetchStartTime;

    private static final String ACCOUNTS_PREFIX = "/v1/accounts";

    public DataProviderResponse fetchData(final RedsysAuthenticationMeans authenticationMeans,
                                          final RedsysAccessMeans accessMeans,
                                          final String providerName,
                                          final UrlFetchDataRequest urlFetchData) throws ProviderFetchDataException, TokenInvalidException {
        final RestTemplateManager restTemplateManager = urlFetchData.getRestTemplateManager();
        final SignatureData signatureData = authenticationMeans.getSigningData(urlFetchData.getSigner());
        RedsysHttpClient httpClient = restTemplateService.createHttpClient(authenticationMeans, restTemplateManager);

        String accessToken = accessMeans.getToken().getAccessToken();
        String consentId = accessMeans.getConsentId();
        String psuIpAddress = urlFetchData.getPsuIpAddress();

        List<ProviderAccountDTO> accounts = new ArrayList<>();

        ResponseAccountsList listAccountReference = new ResponseAccountsList();

        try {
            listAccountReference = httpClient.getAllUserAccounts(
                    accessMeans.getToken().getAccessToken(), accessMeans.getConsentId(), signatureData, psuIpAddress);
        } catch (HttpStatusCodeException e) {
            handleNon2xxResponseInFetchData(e, psuIpAddress);
        }

        for (AccountDetails account : listAccountReference.getAccounts()) {
            try {
                List<ProviderTransactionDTO> transactionsConverted = fetchTransactionsForAccount(httpClient, accessMeans, account,
                        transactionsFetchStartTime.calculate(accessMeans.getConsentAt(), urlFetchData.getTransactionsFetchStartTime()), signatureData, psuIpAddress);
                List<Balance> balances = account.getBalances();
                if (balances == null || balances.isEmpty()) {
                    balances = httpClient.getBalanceForAccount(accessToken, consentId, account.getResourceId(), signatureData, psuIpAddress).getBalances();
                }
                accounts.add(mapperService.toProviderAccountDTO(account, balances, transactionsConverted, providerName));
            } catch (HttpStatusCodeException e) {
                handleNon2xxResponseInFetchData(e, psuIpAddress);
            } catch (RuntimeException e) {
                throw new ProviderFetchDataException(e);
            }
        }


        return new DataProviderResponse(accounts);
    }

    protected List<ProviderTransactionDTO> fetchTransactionsForAccount(final RedsysHttpClient httpClient,
                                                                       final RedsysAccessMeans accessMeans,
                                                                       final AccountDetails account,
                                                                       final Instant transactionsFetchStartTime,
                                                                       final SignatureData signatureData,
                                                                       final String psuIpAddress) {
        // Bankinter requires the same X-Request-ID for all pages of transactions
        String requestTraceIdForAllPages = ExternalTracingUtil.createLastExternalTraceId();

        ResponseAccountTransactions userTransactionForGivenAccount = httpClient.getTransactionForGivenAccount(accessMeans,
                signatureData, psuIpAddress, requestTraceIdForAllPages, account.getResourceId(), transactionsFetchStartTime, bookingStatus);

        List<ProviderTransactionDTO> transactionsConverted = mapTransactionObjectAndAddToProviderTransactionList(userTransactionForGivenAccount, account.getCurrency());

        int pageCounter = 1;
        String currentPage;
        String nextPage = getLinkForNextTransaction(userTransactionForGivenAccount);

        if (StringUtils.isBlank(nextPage)) {
            return transactionsConverted;
        }

        do {
            currentPage = nextPage;
            ResponseAccountTransactions transactions = httpClient.getNextPageOfTransactions(accessMeans,
                    signatureData, psuIpAddress, requestTraceIdForAllPages, nextPage);
            transactionsConverted.addAll(mapTransactionObjectAndAddToProviderTransactionList(transactions, account.getCurrency()));
            nextPage = getLinkForNextTransaction(transactions);
            pageCounter++;
        } while (hasNextPage(pageCounter, currentPage, nextPage));

        return transactionsConverted;
    }

    private boolean hasNextPage(final int pageCounter,
                                final String currentPage,
                                final String nextPage) {
        return pageCounter <= properties.getPaginationLimit()
                && StringUtils.isNotBlank(nextPage)
                && !Objects.equals(currentPage, nextPage);
    }

    private List<ProviderTransactionDTO> mapTransactionObjectAndAddToProviderTransactionList(final ResponseAccountTransactions userTransactionForGivenAccount, String accountCurrency) {

        List<ProviderTransactionDTO> transactionsConverted = new ArrayList<>();
        Optional.ofNullable(userTransactionForGivenAccount.getTransactions())
                .map(Transactions::getBooked)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(trx -> mapperService.toProviderTransactionDTO(trx, TransactionStatus.BOOKED))
                .forEach(transactionsConverted::add);
        Optional.ofNullable(userTransactionForGivenAccount.getTransactions())
                .map(Transactions::getPending)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .map(trx -> mapperService.toProviderTransactionDTO(trx, TransactionStatus.PENDING))
                .forEach(transactionsConverted::add);

        return transactionsConverted;
    }

    private String getLinkForNextTransaction(final ResponseAccountTransactions transaction) {
        return Optional.of(transaction)
                .map(ResponseAccountTransactions::getTransactions)
                .map(Transactions::getLinks)
                .map(LinksAccountsTransactions::getNext)
                .map(LinkReference::getHref)
                .map(url -> url.contains(ACCOUNTS_PREFIX) ? url.substring(url.indexOf(ACCOUNTS_PREFIX)) : "")
                .orElse(StringUtils.EMPTY);
    }
}

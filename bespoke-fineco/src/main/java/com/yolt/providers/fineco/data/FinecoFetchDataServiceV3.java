package com.yolt.providers.fineco.data;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.ais.url.UrlFetchDataRequest;
import com.yolt.providers.common.exception.JsonParseException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.fineco.auth.FinecoAuthenticationMeans;
import com.yolt.providers.fineco.config.FinecoProperties;
import com.yolt.providers.fineco.data.mappers.CurrencyCodeMapperV1;
import com.yolt.providers.fineco.data.mappers.FinecoBalanceMapper;
import com.yolt.providers.fineco.data.mappers.FinecoBalanceMapperV1;
import com.yolt.providers.fineco.data.mappers.account.FinecoCardAccountMapper;
import com.yolt.providers.fineco.data.mappers.account.FinecoCurrentAccountMapper;
import com.yolt.providers.fineco.dto.FinecoAccessMeans;
import com.yolt.providers.fineco.dto.FinecoAccount;
import com.yolt.providers.fineco.dto.FinecoClasses;
import com.yolt.providers.fineco.dto.FinecoFunctions;
import com.yolt.providers.fineco.rest.FinecoHttpClientV2;
import com.yolt.providers.fineco.rest.FinecoRestTemplateServiceV2;
import com.yolt.providers.fineco.v2.dto.*;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.AccountType;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

@Service
@Slf4j
public class FinecoFetchDataServiceV3 {

    private final FinecoRestTemplateServiceV2 restTemplateService;
    private final FinecoProperties properties;
    private final FinecoDataMapperV2 dataMapper;
    private final FinecoCardAccountMapper cardAccountMapper;
    private final FinecoCurrentAccountMapper currentAccountMapper;
    private final Clock clock;

    public FinecoFetchDataServiceV3(FinecoRestTemplateServiceV2 restTemplateService, FinecoProperties properties, Clock clock) {
        CurrencyCodeMapperV1 currencyCodeMapper = new CurrencyCodeMapperV1();
        FinecoBalanceMapper balanceMapper = new FinecoBalanceMapperV1(currencyCodeMapper);
        this.restTemplateService = restTemplateService;
        this.properties = properties;
        this.dataMapper = new FinecoDataMapperV2();
        this.cardAccountMapper = new FinecoCardAccountMapper(balanceMapper, currencyCodeMapper, clock);
        this.currentAccountMapper = new FinecoCurrentAccountMapper(balanceMapper, currencyCodeMapper, clock);
        this.clock = clock;
    }

    public DataProviderResponse fetchData(final UrlFetchDataRequest urlFetchData,
                                          final FinecoAuthenticationMeans authenticationMeans,
                                          final FinecoAccessMeans accessMeans,
                                          final String providerName) throws ProviderFetchDataException, TokenInvalidException {
        List<ProviderAccountDTO> yoltAccounts = new ArrayList<>();

        FinecoHttpClientV2 httpClient = restTemplateService.createHttpClient(authenticationMeans, urlFetchData.getRestTemplateManager());

        fetchRawDataAndMapAccounts(yoltAccounts, httpClient, accessMeans, urlFetchData, providerName);

        return new DataProviderResponse(yoltAccounts);
    }

    private void fetchRawDataAndMapAccounts(final List<ProviderAccountDTO> yoltAccounts,
                                            final FinecoHttpClientV2 httpClient,
                                            final FinecoAccessMeans finecoAccessMeans,
                                            final UrlFetchDataRequest urlFetchData,
                                            final String providerName) throws TokenInvalidException, ProviderFetchDataException {
        String consentId = finecoAccessMeans.getConsentId();
        Set<AccountType> supportedAccountTypes = getSupportedAccountTypes(properties.getConsentUrl() + "/" + consentId, urlFetchData.getPsuIpAddress(), httpClient);

        if (supportedAccountTypes.contains(AccountType.CURRENT_ACCOUNT)) {
            List<FinecoAccount<AccountDetails, TransactionsResponse200, ReadAccountBalanceResponse200>> finecoCurrentAccounts = processAccounts(
                    new FinecoClasses(
                            AccountList.class,
                            AccountDetails.class,
                            TransactionsResponse200.class,
                            ReadAccountBalanceResponse200.class
                    ),
                    httpClient,
                    finecoAccessMeans,
                    properties.getCurrentAccounts(),
                    urlFetchData,
                    new FinecoFunctions<>(
                            CurrentAccount.getAccounts,
                            CurrentAccount.getResourceId,
                            CurrentAccount.getTransactionsAbsoluteUrl
                    )
            );
            dataMapper.mapRawToUnifiedAccount(
                    yoltAccounts,
                    finecoCurrentAccounts,
                    currentAccountMapper,
                    providerName);
        }
        if (supportedAccountTypes.contains(AccountType.CREDIT_CARD)) {

            List<FinecoAccount<CardAccountDetails, CardAccountsTransactionsResponse200, ReadCardAccountBalanceResponse200>> finecoCardAccounts = processAccounts(
                    new FinecoClasses(
                            CardAccountList.class,
                            CardAccountDetails.class,
                            CardAccountsTransactionsResponse200.class,
                            ReadCardAccountBalanceResponse200.class
                    ),
                    httpClient,
                    finecoAccessMeans,
                    properties.getCardAccounts(),
                    urlFetchData,
                    new FinecoFunctions<>(
                            CardAccount.getAccounts,
                            CardAccount.getResourceId,
                            CardAccount.getTransactionsAbsoluteUrl
                    )
            );

            dataMapper.mapRawToUnifiedAccount(
                    yoltAccounts,
                    finecoCardAccounts,
                    cardAccountMapper,
                    providerName);
        }
    }

    private <T, U, V, W> List<FinecoAccount<U, V, W>> processAccounts(
            final FinecoClasses<T, U, V, W> classes,
            final FinecoHttpClientV2 httpClient,
            final FinecoAccessMeans finecoAccessMeans,
            final FinecoProperties.AccountsEndpoints accountsEndpoints,
            final UrlFetchDataRequest urlFetchData,
            final FinecoFunctions<T, U, V> finecoFunctions) throws TokenInvalidException, ProviderFetchDataException {
        String psuIpAddress = urlFetchData.getPsuIpAddress();
        String consentId = finecoAccessMeans.getConsentId();
        Instant consentCreateTime = finecoAccessMeans.getConsentCreateTime();

        Function<T, List<U>> accountsFunction = finecoFunctions.getAccountsFunction();
        Function<U, String> resourceIdFunction = finecoFunctions.getResourceIdFunction();
        Function<V, String> transactionsAbsoluteUrlFunction = finecoFunctions.getTransactionsAbsoluteUrlFunction();

        List<FinecoAccount<U, V, W>> finecoAccounts = new ArrayList<>();
        List<String> resourceIdSingleton = Arrays.asList("");

        try {
            T currentAccountsResponse = httpClient.getAccounts(consentId, psuIpAddress, accountsEndpoints.getAccountsUrl(), classes.getAccounts());
            for (U account : accountsFunction.apply(currentAccountsResponse)) {
                String resourceId = resourceIdFunction.apply(account);
                resourceIdSingleton.set(0, resourceId);
                String transactionsUrl = createTransactionsUrl(resourceId,
                        urlFetchData.getTransactionsFetchStartTime(),
                        consentCreateTime,
                        accountsEndpoints.getTransactionsUrlTemplate());
                List<V> transactions = getTransactionsForAccount(httpClient, consentId, psuIpAddress, transactionsUrl, transactionsAbsoluteUrlFunction, classes.getTransactions());

                String balancesUrl = String.format(accountsEndpoints.getBalancesUrlTemplate(), resourceId);
                W balances = httpClient.getBalancesForAccount(consentId, psuIpAddress, balancesUrl, classes.getBalances());

                FinecoAccount<U, V, W> finecoAccount = new FinecoAccount<>();
                finecoAccount.setAccount(account);
                finecoAccount.setBalances(balances);
                finecoAccount.setTransactions(transactions);

                finecoAccounts.add(finecoAccount);
            }
        } catch (ProviderFetchDataException | HttpClientErrorException | JsonParseException e) {
            throw new ProviderFetchDataException(e);
        }
        return finecoAccounts;
    }

    private Set<AccountType> getSupportedAccountTypes(final String relativeUrl,
                                                      final String psuIpAddress,
                                                      final FinecoHttpClientV2 httpClient) throws TokenInvalidException, ProviderFetchDataException {
        Set<AccountType> supportedAccountTypes = new HashSet<>();
        ConsentInformationResponse200 consentInformation = httpClient.getConsentInformation(psuIpAddress, relativeUrl);
        Optional.ofNullable(consentInformation.getAccess())
                .map(AccountAccess::getAccounts)
                .ifPresent(accounts -> accounts.forEach(account -> {
                    if (account.getMaskedPan() != null) {
                        supportedAccountTypes.add(AccountType.CREDIT_CARD);
                    }
                    if (account.getIban() != null) {
                        supportedAccountTypes.add(AccountType.CURRENT_ACCOUNT);
                    }
                }));
        return supportedAccountTypes;
    }

    private <V> List<V> getTransactionsForAccount(final FinecoHttpClientV2 httpClient,
                                                  final String consentId,
                                                  final String psuIpAddress,
                                                  final String transactionUrl,
                                                  final Function<V, String> transactionsAbsoluteUrlFunction,
                                                  final Class<V> responseType) throws TokenInvalidException, ProviderFetchDataException {
        List<V> transactions = new ArrayList<>();
        V userTransactionForGivenAccount = httpClient.getTransactionsForAccount(consentId, psuIpAddress, transactionUrl, responseType);
        transactions.add(userTransactionForGivenAccount);

        int counter = 1;
        String currentPage = transactionsAbsoluteUrlFunction.apply(userTransactionForGivenAccount);
        String previousPage = null;

        while (checkPaginationLimitAndIfNextPageIsAvailable(counter, currentPage, previousPage)) {
            V nextUserTransactionForGivenAccount = httpClient.getTransactionsForNextAccount(consentId, psuIpAddress, currentPage, responseType);
            transactions.add(nextUserTransactionForGivenAccount);
            previousPage = currentPage;
            currentPage = transactionsAbsoluteUrlFunction.apply(nextUserTransactionForGivenAccount);
            counter++;
        }

        return transactions;
    }

    private boolean checkPaginationLimitAndIfNextPageIsAvailable(final int counter,
                                                                 final String currentPage,
                                                                 final String previousPage) {
        return counter <= properties.getPaginationLimit() && !Objects.equals(currentPage, previousPage)
                && StringUtils.isNotBlank(currentPage);
    }

    private static String mapGenericLinksAccountReportToNextPage(HashMap<String, String> linksAccountReport) {
        return linksAccountReport == null ? null : linksAccountReport.get("next");
    }

    private static String parseUrlFromAbsoluteToRelative(String url) {
        String accountRelativeUrl = parseUrlFromAbsoluteToRelative(url, "/accounts");
        return accountRelativeUrl.isEmpty() ? parseUrlFromAbsoluteToRelative(url, "/card-accounts") : accountRelativeUrl;
    }

    private static String parseUrlFromAbsoluteToRelative(String url, String pattern) {
        int index = url.indexOf(pattern);
        return index != -1 ? url.substring(index) : StringUtils.EMPTY;
    }

    private String createTransactionsUrl(final String resourceId,
                                         final Instant transactionsFetchStartTime,
                                         final Instant consentCreateTime,
                                         final String transactionUrlTemplate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        Instant adjustedTransactionsFetchStartTime = adjustTransactionsFetchStartTime(transactionsFetchStartTime, consentCreateTime);
        String formattedFromBookingDateTime = dateTimeFormatter.format(OffsetDateTime.ofInstant(adjustedTransactionsFetchStartTime, ZoneOffset.UTC));

        // fineco only delivers current and card account information for booked transactions
        return String.format(transactionUrlTemplate, resourceId, "booked", formattedFromBookingDateTime);
    }

    private Instant adjustTransactionsFetchStartTime(final Instant receivedTransactionsFetchStartTime,
                                                     final Instant consentCreateTime) {
        Instant presentTime = Instant.now(clock);
        // According to documentation the valid period for transaction gathering is 90 days
        // [fetching transactions is inclusive] if consent is older than 20 minutes

        long adjustedTransactionsTimeFrameDays = properties.getTransactionsTimeFrameDays() - 1;
        if (consentCreateTime.isAfter(presentTime.minus(properties.getTransactionsTimeFrameMinutes(), ChronoUnit.MINUTES)) ||
                receivedTransactionsFetchStartTime.isAfter(presentTime.minus(adjustedTransactionsTimeFrameDays, ChronoUnit.DAYS))) {
            return receivedTransactionsFetchStartTime;
        }

        return presentTime.minus(adjustedTransactionsTimeFrameDays, ChronoUnit.DAYS);
    }

    private static class CurrentAccount {

        private static final Function<AccountList, List<AccountDetails>> getAccounts = accountList -> accountList.getAccounts();
        private static final Function<AccountDetails, String> getResourceId = accountDetails -> accountDetails.getResourceId();
        private static final Function<TransactionsResponse200, String> getTransactionsAbsoluteUrl = transactionResponse -> Optional.of(transactionResponse)
                .map(TransactionsResponse200::getTransactions)
                .map(AccountReport::getLinks)
                .map(FinecoFetchDataServiceV3::mapGenericLinksAccountReportToNextPage)
                .map(FinecoFetchDataServiceV3::parseUrlFromAbsoluteToRelative)
                .orElse(StringUtils.EMPTY);
    }

    private static class CardAccount {

        private static final Function<CardAccountList, List<CardAccountDetails>> getAccounts = accountList -> accountList.getCardAccounts();
        private static final Function<CardAccountDetails, String> getResourceId = accountDetails -> accountDetails.getResourceId();
        private static final Function<CardAccountsTransactionsResponse200, String> getTransactionsAbsoluteUrl = transactionResponse -> Optional.of(transactionResponse)
                .map(CardAccountsTransactionsResponse200::getCardTransactions)
                .map(CardAccountReport::getLinks)
                .map(FinecoFetchDataServiceV3::mapGenericLinksAccountReportToNextPage)
                .map(FinecoFetchDataServiceV3::parseUrlFromAbsoluteToRelative)
                .orElse(StringUtils.EMPTY);
    }
}
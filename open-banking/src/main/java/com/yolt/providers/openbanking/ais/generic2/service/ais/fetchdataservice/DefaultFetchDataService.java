package com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.EndpointsVersionable;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code.CREDITCARD;

@Slf4j
@RequiredArgsConstructor
@Deprecated //Use newer version with accessMeansState C4PO-8398
public class DefaultFetchDataService implements FetchDataService, EndpointsVersionable {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
    private static final String GET_ACCOUNTS_URL = "/aisp/accounts";
    private static final String GET_BALANCES_URL_TEMPLATE = GET_ACCOUNTS_URL + "/%s/balances";
    private static final String GET_TRANSACTIONS_URL_TEMPLATES = GET_ACCOUNTS_URL + "/%s/transactions?fromBookingDateTime=%s";
    private static final String GET_STANDING_ORDERS_URL_TEMPLATE = GET_ACCOUNTS_URL + "/%s/standing-orders";
    private static final String GET_DIRECT_DEBITS_URL_TEMPLATE = GET_ACCOUNTS_URL + "/%s/direct-debits";

    private final Set<String> supportedAccountsSubtypeNames = new HashSet<>(Arrays.asList("UK.OBIE.IBAN", "UK.OBIE.SORTCODEACCOUNTNUMBER"));
    private final RestClient restClient;
    private final DefaultProperties properties;
    private final Function<OBTransaction6, ProviderTransactionDTO> transactionMapper;
    private final Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper;
    private final Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper;
    private final AccountMapper accountMapper;
    private final UnaryOperator<List<OBAccount6>> accountFilter;
    private final Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier;
    private final Duration consentWindow;
    @Getter
    private final String endpointsVersion;
    private final Clock clock;


    @Override
    public DataProviderResponse getAccountsAndTransactions(final HttpClient httpClient,
                                                           final DefaultAuthMeans authenticationMeans,
                                                           final Instant transactionsFetchStartTime,
                                                           final AccessMeans accessToken) throws TokenInvalidException, ProviderFetchDataException {
        final List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

        String currentPage = getAccountsUrl();
        String nextPage = null;
        int pageCounter = 1;

        do {
            try {
                OBReadAccount6 accountGETResponse = restClient.fetchAccounts(httpClient, getAdjustedUrlPath(currentPage), accessToken,
                        authenticationMeans.getInstitutionId(), OBReadAccount6.class);
                nextPage = accountGETResponse.getLinks().getNext();
                nextPage = adaptNextPageHook(nextPage);

                List<OBAccount6> filteredAccountList = accountFilter.apply(accountGETResponse.getData().getAccount());
                for (OBAccount6 account : filteredAccountList) {
                    checkAndProcessAccount(httpClient, authenticationMeans,
                            narrowTransactionFetchStartTimeHook(transactionsFetchStartTime, accessToken.isInConsentWindow(clock, consentWindow)),
                            accessToken, responseAccounts, account);
                }
            } catch (RestClientException e) {
                throw new ProviderFetchDataException(e);
            }

            // Prevent infinite loop on failure to get nextPage
            // Failed Account will already be added because an exception will be thrown and caught during 'performRequest()' call.
            if (Objects.equals(currentPage, nextPage)) {
                break;
            }
            currentPage = nextPage;
            pageCounter++;
        } while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());

        return new DataProviderResponse(Collections.unmodifiableList(responseAccounts));
    }

    protected void checkAndProcessAccount(final HttpClient httpClient,
                                          final DefaultAuthMeans authenticationMeans,
                                          final Instant transactionsFetchStartTime,
                                          final AccessMeans accessToken,
                                          final List<ProviderAccountDTO> responseAccounts,
                                          final OBAccount6 account) throws TokenInvalidException {
        String externalAccountId = account.getAccountId();
        if (supportedAccountSupplier.get().contains(account.getAccountSubType())) {
            ProviderAccountDTO providerAccountDto = processAccount(httpClient, transactionsFetchStartTime, accessToken,
                    externalAccountId, account, authenticationMeans);
            responseAccounts.add(providerAccountDto);
        }
    }

    protected ProviderAccountDTO processAccount(final HttpClient httpClient,
                                                final Instant transactionsFetchStartTime,
                                                final AccessMeans accessToken,
                                                final String externalAccountId,
                                                final OBAccount6 account,
                                                final DefaultAuthMeans authenticationMeans) throws TokenInvalidException {

        List<ProviderTransactionDTO> transactions = getAllTransactions(account.getAccountSubType(), httpClient, accessToken, externalAccountId,
                transactionsFetchStartTime, authenticationMeans);
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = fetchBalances(httpClient, accessToken,
                externalAccountId, authenticationMeans);

        List<StandingOrderDTO> standingOrders = getStandingOrders(httpClient, accessToken, externalAccountId, authenticationMeans, account.getAccountSubType());
        List<DirectDebitDTO> directDebits = getDirectDebits(httpClient, accessToken, externalAccountId, authenticationMeans, account.getAccountSubType());

        return accountMapper.mapToProviderAccount(account, transactions, balancesByType, standingOrders, directDebits);
    }

    protected List<DirectDebitDTO> getDirectDebits(final HttpClient httpClient,
                                                   final AccessMeans accessToken,
                                                   final String externalAccountId,
                                                   final DefaultAuthMeans authenticationMeans,
                                                   final OBExternalAccountSubType1Code accountSubType) {

        // Use the initial consent windows during which TPP is not chained by restrictions regarding some endpoints
        if (CREDITCARD.equals(accountSubType) || !accessToken.isInConsentWindow(clock, consentWindow)) {
            return Collections.emptyList();
        }
        List<DirectDebitDTO> directDebits = new ArrayList<>();
        String nextPage = String.format(getDirectDebitsUrlTemplate(), externalAccountId);
        int pageCounter = 1;

        try {
            do {
                OBReadDirectDebit2 directDebitsResponse = restClient.fetchDirectDebits(httpClient, getAdjustedUrlPath(nextPage), accessToken,
                        authenticationMeans.getInstitutionId(), OBReadDirectDebit2.class);
                // According to Open Banking it is allowed that the bank leaves out the 'Direct Debits' element if there is none
                if (directDebitsResponse == null || directDebitsResponse.getData() == null || directDebitsResponse.getData().getDirectDebit() == null) { //NOSONAR check for null anyway - it's proven it can be null even when the specs say it's not
                    break;
                }
                List<OBReadDirectDebit2DataDirectDebit> responseDirectDebits = directDebitsResponse.getData().getDirectDebit();
                for (OBReadDirectDebit2DataDirectDebit dd : responseDirectDebits) {
                    Optional<DirectDebitDTO> optionalDirectDebitDTO = directDebitMapper.apply(dd);
                    if (optionalDirectDebitDTO.isPresent()) {
                        DirectDebitDTO directDebitDTO = optionalDirectDebitDTO.get();
                        directDebitDTO.validate();
                        directDebits.add(directDebitDTO);
                    }
                }

                nextPage = directDebitsResponse.getLinks() != null ? directDebitsResponse.getLinks().getNext() : null; //NOSONAR check for null anyway - it's proven it can be null even when the specs say it's not
                nextPage = adaptNextPageHook(nextPage);
                pageCounter++;

            }
            while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());
        } catch (Exception e) {
            // Even if we get an error here, just ignore it because we want the refresh to finish successfully.
        }

        return directDebits;
    }

    protected List<StandingOrderDTO> getStandingOrders(final HttpClient httpClient,
                                                       final AccessMeans accessToken,
                                                       final String externalAccountId,
                                                       final DefaultAuthMeans authenticationMeans,
                                                       final OBExternalAccountSubType1Code accountSubType) {

        // Use the initial consent windows during which TPP is not chained by restrictions regarding some endpoints
        if (CREDITCARD.equals(accountSubType) || !accessToken.isInConsentWindow(clock, consentWindow)) {
            return Collections.emptyList();
        }
        List<StandingOrderDTO> standingOrders = new ArrayList<>();
        String nextPage = String.format(getStandingOrdersUrlTemplate(), externalAccountId);
        int pageCounter = 1;

        try {
            do {
                OBReadStandingOrder6 standingOrdersResponse = restClient.fetchStandingOrders(
                        httpClient, getAdjustedUrlPath(nextPage), accessToken, authenticationMeans.getInstitutionId(), OBReadStandingOrder6.class);

                // According to Open Banking it is allowed that the bank leaves out the 'Standing Order' element if there is none
                if (standingOrdersResponse == null || standingOrdersResponse.getData() == null || standingOrdersResponse.getData().getStandingOrder() == null) { //NOSONAR check for null anyway - it's proven it can be null even when the specs say it's not
                    break;
                }
                List<OBStandingOrder6> responseStandingOrders = standingOrdersResponse.getData().getStandingOrder();
                for (OBStandingOrder6 so : responseStandingOrders) {
                    // According to one version of the spec. Nullified payment amount indicates the inactivity of such standing order.
                    if (so.getNextPaymentAmount() == null) {
                        continue;
                    }
                    StandingOrderDTO standingOrderDTO = standingOrderMapper.apply(so);
                    standingOrderDTO.validate();
                    standingOrders.add(standingOrderDTO);
                }

                nextPage = standingOrdersResponse.getLinks() != null ? standingOrdersResponse.getLinks().getNext() : null; //NOSONAR check for null anyway - it's proven it can be null even when the specs say it's not
                nextPage = adaptNextPageHook(nextPage);
                pageCounter++;

            }
            while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());
        } catch (Exception ignored) {
            // Even if we get an error here, just ignore it because we want the refresh to finish successfully.
        }
        return standingOrders;
    }

    protected Map<OBBalanceType1Code, OBReadBalance1DataBalance> fetchBalances(final HttpClient httpClient,
                                                                               final AccessMeans accessToken,
                                                                               final String externalAccountId,
                                                                               final DefaultAuthMeans authenticationMeans) throws TokenInvalidException {
        List<OBReadBalance1DataBalance> balances = new ArrayList<>();
        String nextPage = String.format(getBalancesUrlTemplate(), externalAccountId);
        int pageCounter = 1;
        do {
            OBReadBalance1 balance = restClient.fetchBalances(httpClient, getAdjustedUrlPath(nextPage), accessToken,
                    authenticationMeans.getInstitutionId(), OBReadBalance1.class);
            if (balanceIsEmpty(balance)) {
                break;
            }
            balances.addAll(balance.getData().getBalance());
            nextPage = balance.getLinks() != null ? balance.getLinks().getNext() : null; //NOSONAR check for null anyway - it's proven it can be null even when the specs say it's not
            nextPage = adaptNextPageHook(nextPage);
            pageCounter++;
        } while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());

        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = new HashMap<>();
        for (OBReadBalance1DataBalance balance : balances) {
            if (balancesByType.put(balance.getType(), balance) != null) {
                return Collections.emptyMap();
            }
        }
        return balancesByType;
    }

    protected List<ProviderTransactionDTO> getAllTransactions(final OBExternalAccountSubType1Code accountSubType,   //NOSONAR Parameter is used in subclasses
                                                              final HttpClient httpClient,
                                                              final AccessMeans accessToken,
                                                              final String externalAccountId,
                                                              final Instant transactionsFetchStartTime,
                                                              final DefaultAuthMeans authenticationMeans) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactions = new ArrayList<>();

        String formattedFromBookingDateTime = formatFromBookingDateTime(transactionsFetchStartTime);

        String nextPage = String.format(getTransactionsUrlTemplates(), externalAccountId, formattedFromBookingDateTime);
        int pageCounter = 1;
        do {
            OBReadTransaction6 accountTransaction = restClient.fetchTransactions(httpClient, getAdjustedUrlPath(nextPage), accessToken,
                    authenticationMeans.getInstitutionId(), OBReadTransaction6.class);

            // According to Open Banking it is allowed that the bank leaves out the 'Transaction' element if there is none
            List<OBTransaction6> responseTransactions = accountTransaction.getData().getTransaction();
            if (responseTransactions == null) {
                break;
            }

            for (OBTransaction6 transaction : responseTransactions) {
                transactions.add(transactionMapper.apply(transaction));
            }

            nextPage = accountTransaction.getLinks() != null ? accountTransaction.getLinks().getNext() : null;
            nextPage = adaptNextPageHook(nextPage);
            pageCounter++;
        } while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());

        return transactions;
    }

    protected Instant narrowTransactionFetchStartTimeHook(Instant transactionFetchStartTime, boolean isInConsentWindow) {
        return transactionFetchStartTime;
    }

    protected String adaptNextPageHook(String url) {
        return url;
    }

    protected String formatFromBookingDateTime(final Instant transactionsFetchStartTime) {
        return DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC));
    }

    protected String getAccountsUrl() {
        return GET_ACCOUNTS_URL;
    }

    protected String getBalancesUrlTemplate() {
        return GET_BALANCES_URL_TEMPLATE;
    }

    protected String getTransactionsUrlTemplates() {
        return GET_TRANSACTIONS_URL_TEMPLATES;
    }

    protected String getStandingOrdersUrlTemplate() {
        return GET_STANDING_ORDERS_URL_TEMPLATE;
    }

    protected String getDirectDebitsUrlTemplate() {
        return GET_DIRECT_DEBITS_URL_TEMPLATE;
    }

    private Boolean balanceIsEmpty(OBReadBalance1 balance) {
        if (balance == null) {
            return true;
        }
        if (balance.getData() == null) { //NOSONAR check for null anyway - it's proven it can be null even when the specs say it's not
            return true;
        }
        //NOSONAR check for null anyway - it's proven it can be null even when the specs say it's not
        return balance.getData().getBalance() == null;
    }
}

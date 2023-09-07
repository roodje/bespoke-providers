package com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.common.EndpointsVersionable;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.exceptionhandler.FetchDataExceptionHandler;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code.CREDITCARD;

@Slf4j
@RequiredArgsConstructor
public class DefaultFetchDataServiceV4 implements FetchDataServiceV2, EndpointsVersionable {

    private static final String GET_ACCOUNTS_URL = "/aisp/accounts";
    private static final String GET_BALANCES_URL_TEMPLATE = GET_ACCOUNTS_URL + "/%s/balances";
    private static final String GET_TRANSACTIONS_URL_TEMPLATES = GET_ACCOUNTS_URL + "/%s/transactions?fromBookingDateTime=%s";
    private static final String GET_STANDING_ORDERS_URL_TEMPLATE = GET_ACCOUNTS_URL + "/%s/standing-orders";
    private static final String GET_DIRECT_DEBITS_URL_TEMPLATE = GET_ACCOUNTS_URL + "/%s/direct-debits";
    private static final String GET_ACCOUNT_PARTIES_URL = "/aisp/accounts/%s/parties";

    private final Set<String> supportedAccountsSubtypeNames = new HashSet<>(Arrays.asList("UK.OBIE.IBAN", "UK.OBIE.SORTCODEACCOUNTNUMBER"));
    private final RestClient restClient;
    private final PartiesRestClient partiesRestClient;
    private final DefaultProperties properties;
    private final Function<OBTransaction6, ProviderTransactionDTO> transactionMapper;
    private final Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper;
    private final Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper;
    private final Function<OBParty2, PartyDto> partiesMapper;
    private final AccountMapperV2 accountMapper;
    private final Function<Instant, String> fromBookingDateTimeFormatter;
    private final UnaryOperator<List<OBAccount6>> accountFilter;
    private final Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier;
    private final Duration consentWindow;
    @Getter
    private final String endpointsVersion;
    private final Clock clock;
    private final FetchDataExceptionHandler fetchAccountsExceptionHandler;
    private final FetchDataExceptionHandler fetchAccountDetailsExceptionHandler;

    @Override
    public DataProviderResponse getAccountsAndTransactions(final HttpClient httpClient,
                                                           final DefaultAuthMeans authenticationMeans,
                                                           final Instant transactionsFetchStartTime,
                                                           final AccessMeansState accessMeansState) throws TokenInvalidException, ProviderFetchDataException {
        final List<ProviderAccountDTO> responseAccounts = new ArrayList<>();
        AccessMeans accessToken = accessMeansState.getAccessMeans();

        String currentPage = getAccountsUrl();
        String nextPage = null;
        int pageCounter = 1;

        //fetch accounts
        List<OBAccount6> fetchedAccount = new ArrayList<>();
        try {
            do {
                OBReadAccount6 accountGETResponse = restClient.fetchAccounts(httpClient, getAdjustedUrlPath(currentPage), accessToken,
                        authenticationMeans.getInstitutionId(), OBReadAccount6.class);
                nextPage = accountGETResponse.getLinks().getNext();
                nextPage = adaptNextPageHook(nextPage);

                fetchedAccount.addAll(accountFilter.apply(accountGETResponse.getData().getAccount()));
                currentPage = nextPage;
                pageCounter++;
            } while (!Objects.equals(currentPage, nextPage) && !StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());

        } catch (RuntimeException e) {
            fetchAccountsExceptionHandler.handleException(e);
        }

        //fetchAccount details
        for (OBAccount6 account : fetchedAccount) {
            try {
                checkAndProcessAccount(httpClient, authenticationMeans,
                        narrowTransactionFetchStartTimeHook(transactionsFetchStartTime, accessToken.isInConsentWindow(clock, consentWindow)),
                        accessMeansState, responseAccounts, account);
            } catch (RuntimeException e) {
                fetchAccountDetailsExceptionHandler.handleException(e);
            }
        }

        return new DataProviderResponse(Collections.unmodifiableList(responseAccounts));
    }

    protected void checkAndProcessAccount(final HttpClient httpClient,
                                          final DefaultAuthMeans authenticationMeans,
                                          final Instant transactionsFetchStartTime,
                                          final AccessMeansState accessMeansState,
                                          final List<ProviderAccountDTO> responseAccounts,
                                          final OBAccount6 account) throws TokenInvalidException {
        String externalAccountId = account.getAccountId();
        if (supportedAccountSupplier.get().contains(account.getAccountSubType())) {
            ProviderAccountDTO providerAccountDto = processAccount(httpClient, transactionsFetchStartTime, accessMeansState,
                    externalAccountId, account, authenticationMeans);
            responseAccounts.add(providerAccountDto);
        }
    }

    protected ProviderAccountDTO processAccount(final HttpClient httpClient,
                                                final Instant transactionsFetchStartTime,
                                                final AccessMeansState accessMeansState,
                                                final String externalAccountId,
                                                final OBAccount6 account,
                                                final DefaultAuthMeans authenticationMeans) throws TokenInvalidException {
        AccessMeans accessToken = accessMeansState.getAccessMeans();
        List<ProviderTransactionDTO> transactions = getAllTransactions(account.getAccountSubType(), httpClient, accessToken, externalAccountId,
                transactionsFetchStartTime, authenticationMeans);
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = fetchBalances(httpClient, accessToken,
                externalAccountId, authenticationMeans);

        List<StandingOrderDTO> standingOrders = getStandingOrders(httpClient, accessToken, externalAccountId, authenticationMeans, account.getAccountSubType());
        List<DirectDebitDTO> directDebits = getDirectDebits(httpClient, accessToken, externalAccountId, authenticationMeans, account.getAccountSubType());
        List<PartyDto> parties = getParties(httpClient, accessMeansState, externalAccountId, authenticationMeans);
        return accountMapper.mapToProviderAccount(account, transactions, balancesByType, standingOrders, directDebits, parties);
    }

    protected List<PartyDto> getParties(HttpClient httpClient, AccessMeansState accessMeansState, String externalAccountId, DefaultAuthMeans authenticationMeans) {

        //error handling and paging
        if (!accessMeansState.getAccessMeans().isInConsentWindow(clock, consentWindow) || !shouldCallPartiesEndpoint(accessMeansState)) {
            return Collections.EMPTY_LIST;
        }

        List<PartyDto> parties = new ArrayList<>();
        String nextPage = String.format(getAccountPartiesUrlTemplate(), externalAccountId);
        int pageCounter = 1;
        try {
            do {
                OBReadParty3 partiesResponse = partiesRestClient.callForParties(httpClient, getAdjustedUrlPath(nextPage),
                        accessMeansState.getAccessMeans(), authenticationMeans, OBReadParty3.class);

                parties.addAll(partiesResponse.getData().getParty()
                        .stream().map(partiesMapper::apply).collect(Collectors.toList()));

                nextPage = partiesResponse.getLinks() != null ? partiesResponse.getLinks().getNext() : null;
                nextPage = adaptNextPageHook(nextPage);
                pageCounter++;
            } while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());
        } catch (Exception e) {
            // Even if we get an error here, just ignore it because we want the refresh to finish successfully
        }
        return parties;
    }

    //Use composition
    private boolean shouldCallPartiesEndpoint(AccessMeansState accessMeansState) {
        return accessMeansState.getPermissions().contains(OBReadConsent1Data.PermissionsEnum.READPARTY.toString());
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

        String formattedFromBookingDateTime = fromBookingDateTimeFormatter.apply(transactionsFetchStartTime);

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

    protected String getAccountPartiesUrlTemplate() {
        return GET_ACCOUNT_PARTIES_URL;
    }

    protected String getDirectDebitsUrlTemplate() {
        return GET_DIRECT_DEBITS_URL_TEMPLATE;
    }

    protected Boolean balanceIsEmpty(OBReadBalance1 balance) {
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

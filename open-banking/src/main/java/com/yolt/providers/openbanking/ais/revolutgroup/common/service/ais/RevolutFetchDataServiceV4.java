package com.yolt.providers.openbanking.ais.revolutgroup.common.service.ais;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.ais.revolutgroup.common.enums.RevolutForbiddenCurrencyCode;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class RevolutFetchDataServiceV4 extends DefaultFetchDataService {

    private static final String GET_ACCOUNTS_URL = "/accounts";
    private static final String GET_BALANCES_URL_TEMPLATE = GET_ACCOUNTS_URL + "/%s/balances";
    private static final String GET_TRANSACTIONS_URL_TEMPLATES = GET_ACCOUNTS_URL + "/%s/transactions?fromBookingDateTime=%s";
    private static final String GET_STANDING_ORDERS_URL_TEMPLATE = GET_ACCOUNTS_URL + "/%s/standing-orders";
    private static final String GET_DIRECT_DEBITS_URL_TEMPLATE = GET_ACCOUNTS_URL + "/%s/direct-debits";

    private final DefaultProperties properties;
    private final RestClient restClient;
    private final Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier;
    private final Duration consentWindow;
    private final UnaryOperator<List<OBAccount6>> accountFilter;
    private final Clock clock;

    public RevolutFetchDataServiceV4(RestClient restClient,
                                     DefaultProperties properties,
                                     Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                     Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                     Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                     AccountMapper accountMapper,
                                     UnaryOperator<List<OBAccount6>> accountFilter,
                                     Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                     Duration consentWindow,
                                     String endpointsVersion,
                                     Clock clock) {
        super(restClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, accountMapper, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock);
        this.properties = properties;
        this.restClient = restClient;
        this.accountFilter = accountFilter;
        this.supportedAccountSupplier = supportedAccountSupplier;
        this.consentWindow = consentWindow;
        this.clock = clock;
    }

    @Override
    protected String getAccountsUrl() {
        return GET_ACCOUNTS_URL;
    }

    @Override
    protected String getBalancesUrlTemplate() {
        return GET_BALANCES_URL_TEMPLATE;
    }

    @Override
    protected String getTransactionsUrlTemplates() {
        return GET_TRANSACTIONS_URL_TEMPLATES;
    }

    @Override
    protected String getStandingOrdersUrlTemplate() {
        return GET_STANDING_ORDERS_URL_TEMPLATE;
    }

    @Override
    protected String getDirectDebitsUrlTemplate() {
        return GET_DIRECT_DEBITS_URL_TEMPLATE;
    }

    /**
     * This method has been overridden because Revolut returns null
     * instead of empty accounts list which is a cause of NPE
     */
    @Override
    public DataProviderResponse getAccountsAndTransactions(HttpClient httpClient, DefaultAuthMeans authenticationMeans, Instant transactionsFetchStartTime, AccessMeans accessToken) throws TokenInvalidException, ProviderFetchDataException {
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

                OBReadAccount6Data accountsData = accountGETResponse.getData();
                if (accountsData == null || accountsData.getAccount() == null) {
                    throw new ProviderFetchDataException("Received empty accounts list");
                }
                List<OBAccount6> filteredAccountList = accountFilter.apply(accountsData.getAccount());
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
        if (supportedAccountSupplier.get().contains(account.getAccountSubType())
                && accountCurrencyAllowed(account.getCurrency())) {
            ProviderAccountDTO providerAccountDto = processAccount(httpClient, transactionsFetchStartTime, accessToken,
                    externalAccountId, account, authenticationMeans);
            responseAccounts.add(providerAccountDto);
        }
    }

    private boolean accountCurrencyAllowed(String currency) {
        return Arrays.stream(RevolutForbiddenCurrencyCode.values())
                .noneMatch(currencyCode -> currencyCode.name().equals(currency));
    }

    // IT'S NOT IMPLEMENTED AT THEIR SIDE
    @Override
    protected List<DirectDebitDTO> getDirectDebits(final HttpClient httpClient,
                                                   final AccessMeans accessToken,
                                                   final String externalAccountId,
                                                   final DefaultAuthMeans authenticationMeans,
                                                   final OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }

    // IT'S NOT IMPLEMENTED AT THEIR SIDE
    @Override
    protected List<StandingOrderDTO> getStandingOrders(final HttpClient httpClient,
                                                       final AccessMeans accessToken,
                                                       final String externalAccountId,
                                                       final DefaultAuthMeans authenticationMeans,
                                                       final OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }
}
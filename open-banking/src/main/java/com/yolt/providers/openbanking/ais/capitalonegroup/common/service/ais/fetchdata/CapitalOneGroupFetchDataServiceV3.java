package com.yolt.providers.openbanking.ais.capitalonegroup.common.service.ais.fetchdata;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.JsonParseException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.capitalonegroup.common.service.ais.restclient.CapitalOneGroupRestClientV2;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
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
import java.util.stream.Collectors;


public class CapitalOneGroupFetchDataServiceV3 extends DefaultFetchDataService {
    //This class implements some methods with additional parameter (psuIp) thus it DOES NOT implement FetchDataService! C4PO-7841

    private static final String GET_TRANSACTIONS_URL_TEMPLATES = "/aisp/accounts/%s/transactions?fromBookingDateTime=%s&toBookingDateTime=%s";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    private final CapitalOneGroupRestClientV2 restClient;
    private final DefaultProperties properties;
    private final Function<OBTransaction6, ProviderTransactionDTO> transactionMapper;
    private final DefaultAccountMapper accountMapper;
    private final UnaryOperator<List<OBAccount6>> accountFilter;
    private final Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier;
    private final Duration consentWindow;
    private final Clock clock;

    public CapitalOneGroupFetchDataServiceV3(CapitalOneGroupRestClientV2 restClient,
                                             DefaultProperties properties,
                                             Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                             Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                             Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                             DefaultAccountMapper accountMapper,
                                             UnaryOperator<List<OBAccount6>> accountFilter,
                                             Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                             Duration consentWindow,
                                             String endpointsVersion,
                                             Clock clock) {
        super(restClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, accountMapper, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock);
        this.restClient = restClient;
        this.properties = properties;
        this.transactionMapper = transactionMapper;
        this.accountMapper = accountMapper;
        this.accountFilter = accountFilter;
        this.supportedAccountSupplier = supportedAccountSupplier;
        this.consentWindow = consentWindow;
        this.clock = clock;
    }

    protected Map<OBBalanceType1Code, OBReadBalance1DataBalance> fetchBalances(HttpClient httpClient,
                                                                               AccessMeans accessToken,
                                                                               String externalAccountId,
                                                                               String psuIpAddress,
                                                                               DefaultAuthMeans authenticationMeans) throws TokenInvalidException {
        List<OBReadBalance1DataBalance> balances = new ArrayList<>();
        String nextPage = String.format(getBalancesUrlTemplate(), externalAccountId);
        int pageCounter = 1;
        do {
            OBReadBalance1 balance = restClient.fetchBalances(httpClient, getAdjustedUrlPath(nextPage), accessToken,
                    authenticationMeans.getInstitutionId(), psuIpAddress);
            balances.addAll(balance.getData().getBalance());
            nextPage = balance.getLinks() != null ? balance.getLinks().getNext() : null; //NOSONAR check for null anyway - it's proven it can be null even when the specs say it's not
            pageCounter++;
        } while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());

        return balances.stream()
                .collect(Collectors.toMap(OBReadBalance1DataBalance::getType, balance -> balance));
    }

    public DataProviderResponse getAccountsAndTransactions(HttpClient httpClient,
                                                           DefaultAuthMeans authenticationMeans,
                                                           Instant transactionsFetchStartTime,
                                                           AccessMeans accessToken,
                                                           String psuIpAddress) throws TokenInvalidException, ProviderFetchDataException {
        final List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

        String currentPage = getAccountsUrl();
        String nextPage = null;
        int pageCounter = 1;

        do {
            try {
                OBReadAccount6 accountGETResponse = restClient.fetchAccounts(httpClient, getAdjustedUrlPath(currentPage), accessToken,
                        authenticationMeans.getInstitutionId(), OBReadAccount6.class, psuIpAddress);
                nextPage = accountGETResponse.getLinks().getNext();

                List<OBAccount6> filteredAccountList = accountFilter.apply(accountGETResponse.getData().getAccount());
                for (OBAccount6 account : filteredAccountList) {
                    checkAndProcessAccount(httpClient, authenticationMeans,
                            narrowTransactionFetchStartTimeHook(transactionsFetchStartTime, accessToken.isInConsentWindow(clock, consentWindow)),
                            accessToken, responseAccounts, account, psuIpAddress);
                }
            } catch (JsonParseException | RestClientException | MissingDataException e) {
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

    private String checkAndProcessAccount(final HttpClient httpClient,
                                          final DefaultAuthMeans authenticationMeans,
                                          final Instant transactionsFetchStartTime,
                                          final AccessMeans accessToken,
                                          final List<ProviderAccountDTO> responseAccounts,
                                          final OBAccount6 account,
                                          final String psuIpAddress) throws TokenInvalidException {
        String externalAccountId = account.getAccountId();
        if (supportedAccountSupplier.get().contains(account.getAccountSubType())) {
            ProviderAccountDTO providerAccountDto = processAccount(httpClient, transactionsFetchStartTime,
                    psuIpAddress, accessToken, externalAccountId, account, authenticationMeans);
            responseAccounts.add(providerAccountDto);
        }
        return externalAccountId;
    }

    protected ProviderAccountDTO processAccount(final HttpClient httpClient,
                                                final Instant transactionsFetchStartTime,
                                                final String psuIpAddress,
                                                final AccessMeans accessToken,
                                                final String externalAccountId,
                                                final OBAccount6 account,
                                                final DefaultAuthMeans authenticationMeans) throws TokenInvalidException {

        List<ProviderTransactionDTO> transactions = getAllTransactions(httpClient, accessToken, externalAccountId,
                transactionsFetchStartTime, psuIpAddress, authenticationMeans);
        Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType = fetchBalances(httpClient, accessToken,
                externalAccountId, psuIpAddress, authenticationMeans);

        List<StandingOrderDTO> standingOrders = getStandingOrders(httpClient, accessToken, externalAccountId, authenticationMeans, account.getAccountSubType());
        List<DirectDebitDTO> directDebits = getDirectDebits(httpClient, accessToken, externalAccountId, authenticationMeans, account.getAccountSubType());

        return accountMapper.mapToProviderAccount(account, transactions, balancesByType, standingOrders, directDebits);
    }

    // According to email received from bank, CapitalOne does NOT support pagination
    private List<ProviderTransactionDTO> getAllTransactions(HttpClient httpClient,
                                                            AccessMeans accessToken,
                                                            String externalAccountId,
                                                            Instant transactionsFetchStartTime,
                                                            String psuIpAddress,
                                                            DefaultAuthMeans authenticationMeans) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactions = new ArrayList<>();

        String formattedFromBookingDateTime = formatBookingDateTime(transactionsFetchStartTime);
        String formattedToBookingDateTime = formatBookingDateTime(Instant.now(clock));

        String nextPage = String.format(GET_TRANSACTIONS_URL_TEMPLATES, externalAccountId, formattedFromBookingDateTime, formattedToBookingDateTime);
        OBReadTransaction6 accountTransaction = restClient.fetchTransactions(httpClient, getAdjustedUrlPath(nextPage), accessToken,
                authenticationMeans.getInstitutionId(), OBReadTransaction6.class, psuIpAddress);

        List<OBTransaction6> responseTransactions = accountTransaction.getData().getTransaction();

        for (OBTransaction6 transaction : responseTransactions) {
            transactions.add(transactionMapper.apply(transaction));
        }
        return transactions;
    }

    @Override
    protected List<StandingOrderDTO> getStandingOrders(HttpClient httpClient,
                                                       AccessMeans accessToken,
                                                       String externalAccountId,
                                                       DefaultAuthMeans authenticationMeans,
                                                       OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }

    @Override
    protected List<DirectDebitDTO> getDirectDebits(HttpClient httpClient,
                                                   AccessMeans accessToken,
                                                   String externalAccountId,
                                                   DefaultAuthMeans authenticationMeans,
                                                   OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }

    private String formatBookingDateTime(Instant transactionFetchDateTime) {
        return DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(transactionFetchDateTime, ZoneOffset.UTC));
    }
}

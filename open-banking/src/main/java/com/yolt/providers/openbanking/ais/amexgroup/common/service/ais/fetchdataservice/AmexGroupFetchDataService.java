package com.yolt.providers.openbanking.ais.amexgroup.common.service.ais.fetchdataservice;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.exceptionhandler.FetchDataExceptionHandler;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataServiceV4;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
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

public class AmexGroupFetchDataService extends DefaultFetchDataServiceV4 {

    private final RestClient restClient;
    private final UnaryOperator<List<OBAccount6>> accountFilter;
    private final DefaultProperties properties;
    private final Duration consentWindow;
    private final Clock clock;
    private final FetchDataExceptionHandler fetchAccountsExceptionHandler;
    private final FetchDataExceptionHandler fetchAccountDetailsExceptionHandler;
    private final Function<Instant, String> fromBookingDateTimeFormatter;
    private final Function<OBTransaction6, ProviderTransactionDTO> transactionMapper;

    public AmexGroupFetchDataService(RestClient restClient,
                                     PartiesRestClient partiesRestClient,
                                     DefaultProperties properties,
                                     Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                     Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                     Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                     Function<OBParty2, PartyDto> partiesMapper,
                                     AccountMapperV2 accountMapper,
                                     Function<Instant, String> fromBookingDateTimeFormatter,
                                     UnaryOperator<List<OBAccount6>> accountFilter,
                                     Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                     Duration consentWindow,
                                     String endpointsVersion,
                                     Clock clock,
                                     FetchDataExceptionHandler fetchAccountsExceptionHandler,
                                     FetchDataExceptionHandler fetchAccountDetailsExceptionHandler) {

        super(restClient, partiesRestClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, partiesMapper, accountMapper, fromBookingDateTimeFormatter, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock, fetchAccountsExceptionHandler, fetchAccountDetailsExceptionHandler);
        this.restClient = restClient;
        this.accountFilter = accountFilter;
        this.properties = properties;
        this.consentWindow = consentWindow;
        this.clock = clock;
        this.fetchAccountsExceptionHandler = fetchAccountsExceptionHandler;
        this.fetchAccountDetailsExceptionHandler = fetchAccountDetailsExceptionHandler;
        this.fromBookingDateTimeFormatter = fromBookingDateTimeFormatter;
        this.transactionMapper = transactionMapper;
    }

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
                        authenticationMeans.getClientId(), OBReadAccount6.class);
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

    @Override
    protected List<ProviderTransactionDTO> getAllTransactions(OBExternalAccountSubType1Code accountSubType, HttpClient httpClient, AccessMeans accessToken, String externalAccountId, Instant transactionsFetchStartTime, DefaultAuthMeans authenticationMeans) throws TokenInvalidException {
        List<ProviderTransactionDTO> transactions = new ArrayList<>();

        String formattedFromBookingDateTime = fromBookingDateTimeFormatter.apply(transactionsFetchStartTime);

        String nextPage = String.format(getTransactionsUrlTemplates(), externalAccountId, formattedFromBookingDateTime);
        int pageCounter = 1;
        do {
            OBReadTransaction6 accountTransaction = restClient.fetchTransactions(httpClient, getAdjustedUrlPath(nextPage), accessToken,
                    authenticationMeans.getClientId(), OBReadTransaction6.class);

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

    @Override
    protected Map<OBBalanceType1Code, OBReadBalance1DataBalance> fetchBalances(final HttpClient httpClient,
                                                                               final AccessMeans accessToken,
                                                                               final String externalAccountId,
                                                                               final DefaultAuthMeans authenticationMeans) throws TokenInvalidException {
        List<OBReadBalance1DataBalance> balances = new ArrayList<>();
        String nextPage = String.format(getBalancesUrlTemplate(), externalAccountId);
        int pageCounter = 1;
        do {
            OBReadBalance1 balance = restClient.fetchBalances(httpClient, getAdjustedUrlPath(nextPage), accessToken,
                    authenticationMeans.getClientId(), OBReadBalance1.class);
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

    @Override
    protected List<PartyDto> getParties(HttpClient httpClient, AccessMeansState accessMeansState, String externalAccountId, DefaultAuthMeans authenticationMeans) {
        return Collections.emptyList();
    }

    @Override
    protected List<DirectDebitDTO> getDirectDebits(HttpClient httpClient, AccessMeans accessToken, String externalAccountId, DefaultAuthMeans authenticationMeans, OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }

    @Override
    protected List<StandingOrderDTO> getStandingOrders(HttpClient httpClient, AccessMeans accessToken, String externalAccountId, DefaultAuthMeans authenticationMeans, OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }
}

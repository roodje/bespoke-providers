package com.yolt.providers.openbanking.ais.monzogroup.common.service.ais.fetchdataservice;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.JsonParseException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapper;
import com.yolt.providers.openbanking.ais.monzogroup.common.dto.Pot;
import com.yolt.providers.openbanking.ais.monzogroup.common.dto.ReadPot;
import com.yolt.providers.openbanking.ais.monzogroup.common.dto.SupplementaryDataV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.ais.potmapper.MonzoGroupPotMapperV2;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.restclient.MonzoGroupAisRestClientV3;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class MonzoGroupFetchDataServiceV4 extends DefaultFetchDataService {

    private static final String GET_POTS_URL = "/aisp/pots";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final MonzoGroupAisRestClientV3 restClient;
    private final UnaryOperator<List<OBAccount6>> accountFilter;
    private final Duration consentWindow;
    private final DefaultProperties properties;
    private final MonzoGroupPotMapperV2 potMapper;
    private final Function<OBTransaction6, ProviderTransactionDTO> transactionMapper;
    private final Clock clock;

    public MonzoGroupFetchDataServiceV4(MonzoGroupAisRestClientV3 restClient,
                                        DefaultProperties properties,
                                        Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                        Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                        Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                        AccountMapper accountMapper, UnaryOperator<List<OBAccount6>> accountFilter,
                                        Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                        Duration consentWindow,
                                        String endpointsVersion,
                                        MonzoGroupPotMapperV2 potMapper,
                                        Clock clock) {
        super(restClient, properties, transactionMapper, directDebitMapper, standingOrderMapper,
                accountMapper, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock);
        this.restClient = restClient;
        this.accountFilter = accountFilter;
        this.consentWindow = consentWindow;
        this.properties = properties;
        this.potMapper = potMapper;
        this.transactionMapper = transactionMapper;
        this.clock = clock;
    }

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

        ReadPot potObject = restClient.fetchPots(httpClient, getAdjustedUrlPath(GET_POTS_URL), accessToken,
                authenticationMeans.getInstitutionId(), ReadPot.class);

        addPotsToFetchedAccounts(responseAccounts, potObject);

        return new DataProviderResponse(Collections.unmodifiableList(responseAccounts));
    }

    @Override
    protected Instant narrowTransactionFetchStartTimeHook(Instant transactionFetchStartTime, boolean isInConsentWindow) {
        final Instant threeMonthsAgo = Instant.now(clock).minus(Period.ofDays(89));
        if (!isInConsentWindow && transactionFetchStartTime.isBefore(threeMonthsAgo)) {
            return threeMonthsAgo;
        }
        return transactionFetchStartTime;
    }

    @Override
    protected List<ProviderTransactionDTO> getAllTransactions(OBExternalAccountSubType1Code accountSubType,
                                                              HttpClient httpClient,
                                                              AccessMeans accessToken,
                                                              String externalAccountId,
                                                              Instant transactionsFetchStartTime,
                                                              DefaultAuthMeans authenticationMeans) throws TokenInvalidException {
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

            //Monzo register new transaction for balances checks
            //We do not want to show them to users, that is why we filter them out here
            //The same applies to Declined transactions hence the checking
            responseTransactions.removeIf(this::shouldRemoveTransaction);

            for (OBTransaction6 transaction : responseTransactions) {
                transactions.add(transactionMapper.apply(transaction));
            }

            nextPage = accountTransaction.getLinks().getNext();
            nextPage = adaptNextPageHook(nextPage);
            pageCounter++;
        } while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());

        return transactions;
    }

    protected String formatFromBookingDateTime(Instant transactionsFetchStartTime) {
        return DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC));
    }

    private void addPotsToFetchedAccounts(List<ProviderAccountDTO> responseAccounts,
                                          ReadPot potObject) throws ProviderFetchDataException {
        if (checkIfPotObjectContainsPots(potObject)) {
            for (Pot pot : potObject.getData().getPots()) {
                try {
                    ProviderAccountDTO providerAccountDtoPot = potMapper.mapToProviderAccount(pot);
                    responseAccounts.add(providerAccountDtoPot);
                } catch (JsonParseException | RestClientException | MissingDataException e) {
                    throw new ProviderFetchDataException(e);
                }
            }
        }
    }

    private boolean checkIfPotObjectContainsPots(ReadPot pots) {
        return pots != null && pots.getData() != null && pots.getData().getPots() != null;
    }

    private boolean shouldRemoveTransaction(OBTransaction6 transaction) {
        return ObjectUtils.isEmpty(transaction.getAmount().getAmount()) || isZeroAmount(transaction) || isDeclined(transaction);
    }

    private boolean isZeroAmount(OBTransaction6 transaction) {
        DecimalFormat instance = (DecimalFormat) DecimalFormat.getInstance(Locale.UK);
        instance.setParseBigDecimal(true);
        String amount = transaction.getAmount().getAmount();
        try {
            return BigDecimal.ZERO.compareTo((BigDecimal) instance.parse(amount)) == 0;
        } catch (ParseException e) {
            return true;
        }
    }

    private boolean isDeclined(OBTransaction6 transaction) {
        if (transaction.getSupplementaryData() != null && transaction.getSupplementaryData() instanceof SupplementaryDataV2) {
            return ((SupplementaryDataV2) transaction.getSupplementaryData()).getDeclined();
        }

        return false;
    }
}

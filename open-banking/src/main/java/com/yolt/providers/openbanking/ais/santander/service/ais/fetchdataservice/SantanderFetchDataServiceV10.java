package com.yolt.providers.openbanking.ais.santander.service.ais.fetchdataservice;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class SantanderFetchDataServiceV10 extends DefaultFetchDataServiceV2 {

    private static final String ACCOUNT_BLOCKED_MESSAGE = "Account, or Accounts have blocks preventing customer details being shared";

    private final RestClient restClient;
    private final UnaryOperator<List<OBAccount6>> accountFilter;
    private final Duration consentWindow;
    private final Clock clock;
    private final DefaultProperties properties;

    public SantanderFetchDataServiceV10(RestClient restClient,
                                        PartiesRestClient partiesRestClient,
                                        DefaultProperties properties,
                                        Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                        Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                        Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                        Function<OBParty2, PartyDto> partiesMapper,
                                        AccountMapperV2 accountMapper, UnaryOperator<List<OBAccount6>> accountFilter,
                                        Supplier<Set<OBExternalAccountSubType1Code>> getSupportedAccountSubtypes,
                                        Duration consentWindow,
                                        String endpointsVersion,
                                        Clock clock) {
        super(restClient, partiesRestClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, partiesMapper,
                accountMapper, accountFilter, getSupportedAccountSubtypes, consentWindow, endpointsVersion, clock);
        this.restClient = restClient;
        this.accountFilter = accountFilter;
        this.consentWindow = consentWindow;
        this.clock = clock;
        this.properties = properties;
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

        do {
            try {
                OBReadAccount6 accountGETResponse = restClient.fetchAccounts(httpClient, getAdjustedUrlPath(currentPage), accessToken,
                        authenticationMeans.getInstitutionId(), OBReadAccount6.class);
                nextPage = accountGETResponse.getLinks().getNext();
                nextPage = adaptNextPageHook(nextPage);

                List<OBAccount6> filteredAccountList = accountFilter.apply(accountGETResponse.getData().getAccount());
                for (OBAccount6 account : filteredAccountList) {
                    try {
                        checkAndProcessAccount(httpClient, authenticationMeans,
                                narrowTransactionFetchStartTimeHook(transactionsFetchStartTime, accessToken.isInConsentWindow(clock, consentWindow)),
                                accessMeansState, responseAccounts, account);
                    } catch (HttpClientErrorException.Forbidden e) {
                        if (!e.getResponseBodyAsString().contains(ACCOUNT_BLOCKED_MESSAGE)) {
                            throw e;
                        }
                    }
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

    @Override
    protected String formatFromBookingDateTime(Instant transactionsFetchStartTime) {
        return transactionsFetchStartTime.truncatedTo(ChronoUnit.MILLIS).toString();
    }
}

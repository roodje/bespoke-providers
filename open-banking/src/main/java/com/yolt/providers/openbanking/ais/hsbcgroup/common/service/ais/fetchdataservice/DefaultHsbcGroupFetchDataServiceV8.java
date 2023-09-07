package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.JsonParseException;
import com.yolt.providers.common.exception.MissingDataException;
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
import com.yolt.providers.openbanking.ais.hsbcgroup.common.service.ais.fetchdataservice.errorhandler.ClosedAndBlockedAccountErrorHandler;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code.SAVINGS;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;

// After migration to ob 3.1.6 only differences from generic are:
// error handling in fetchdata
// not returning directdebits and standing orders for saving accounts
// narrowing transactionFetchStartTime to up 89 days
public class DefaultHsbcGroupFetchDataServiceV8 extends DefaultFetchDataServiceV2 {

    private final RestClient restClient;
    private final UnaryOperator<List<OBAccount6>> accountFilter;
    private final Duration consentWindow;
    private final ObjectMapper objectMapper;
    private final HsbcFetchDataTimeNarrower fetchDataTimeNarrower;
    private final Clock clock;
    private DefaultProperties properties;
    ClosedAndBlockedAccountErrorHandler closeAndBlockedAccountErrorHandler;

    public DefaultHsbcGroupFetchDataServiceV8(RestClient restClient,
                                              PartiesRestClient partiesRestClient,
                                              DefaultProperties properties,
                                              Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                              Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                              Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                              Function<OBParty2, PartyDto> partiesMapper,
                                              AccountMapperV2 accountMapper,
                                              UnaryOperator<List<OBAccount6>> accountFilter,
                                              Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                              ObjectMapper objectMapper,
                                              Duration consentWindow,
                                              String endpointsVersion,
                                              Clock clock,
                                              HsbcFetchDataTimeNarrower hsbcFetchDataTimeNarrower,
                                              ClosedAndBlockedAccountErrorHandler closedAndBlockedAccountErrorHandler) {
        super(restClient, partiesRestClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, partiesMapper, accountMapper, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock);

        this.restClient = restClient;
        this.properties = properties;
        this.accountFilter = accountFilter;
        this.consentWindow = consentWindow;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.fetchDataTimeNarrower = hsbcFetchDataTimeNarrower;
        this.closeAndBlockedAccountErrorHandler = closedAndBlockedAccountErrorHandler;
    }

    @Override
    public DataProviderResponse getAccountsAndTransactions(final HttpClient httpClient,
                                                           final DefaultAuthMeans authenticationMeans,
                                                           final Instant transactionsFetchStartTime,
                                                           final AccessMeansState accessMeansState) throws TokenInvalidException, ProviderFetchDataException {
        final List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

        String currentPage = getAccountsUrl();
        String nextPage = null;
        int pageCounter = 1;

        do {
            try {
                OBReadAccount6 accountGETResponse = restClient.fetchAccounts(httpClient, getAdjustedUrlPath(currentPage), accessMeansState.getAccessMeans(),
                        authenticationMeans.getInstitutionId(), OBReadAccount6.class);
                nextPage = accountGETResponse.getLinks().getNext();
                nextPage = adaptNextPageHook(nextPage);

                List<OBAccount6> filteredAccountList = accountFilter.apply(accountGETResponse.getData().getAccount());
                for (OBAccount6 account : filteredAccountList) {
                    try {
                        checkAndProcessAccount(httpClient, authenticationMeans,
                                narrowTransactionFetchStartTimeHook(transactionsFetchStartTime, accessMeansState.getAccessMeans().isInConsentWindow(clock, consentWindow)),
                                accessMeansState, responseAccounts, account);
                    } catch (HttpStatusCodeException e) {
                        closeAndBlockedAccountErrorHandler.handle(e);
                    }

                }
            } catch (HttpStatusCodeException e) {
                handleStatusCodeException(e);
                throw new ProviderFetchDataException(e);
            } catch (RestClientException | MissingDataException | JsonParseException e) {
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

    private void handleStatusCodeException(HttpStatusCodeException e) throws TokenInvalidException {
        if (BAD_REQUEST.equals(e.getStatusCode())) {
            try {
                OBErrorResponse1 obErrorResponse1 = objectMapper.readValue(e.getResponseBodyAsString(), OBErrorResponse1.class);

                if (obErrorResponse1.getErrors().stream().anyMatch(errorCode -> errorCode.getErrorCode().equals("UK.OBIE.Resource.InvalidConsentStatus"))) {
                    throw new TokenInvalidException("UK.OBIE.Resource.InvalidConsentStatus");
                }
            } catch (JsonProcessingException ignored) {
            }
        }

        if (FORBIDDEN.equals(e.getStatusCode())) {
            throw new TokenInvalidException(e.getMessage());
        }
    }

    @Override
    protected List<DirectDebitDTO> getDirectDebits(final HttpClient httpClient,
                                                   final AccessMeans accessToken,
                                                   final String externalAccountId,
                                                   final DefaultAuthMeans authenticationMeans,
                                                   final OBExternalAccountSubType1Code accountSubType) {
        if (SAVINGS.equals(accountSubType)) {
            return Collections.emptyList();
        }
        return super.getDirectDebits(httpClient, accessToken, externalAccountId, authenticationMeans, accountSubType);

    }

    @Override
    protected List<StandingOrderDTO> getStandingOrders(final HttpClient httpClient,
                                                       final AccessMeans accessToken,
                                                       final String externalAccountId,
                                                       final DefaultAuthMeans authenticationMeans,
                                                       final OBExternalAccountSubType1Code accountSubType) {
        if (SAVINGS.equals(accountSubType)) {
            return Collections.emptyList();
        }
        return super.getStandingOrders(httpClient, accessToken, externalAccountId, authenticationMeans, accountSubType);

    }

    @Override
    protected Instant narrowTransactionFetchStartTimeHook(Instant transactionFetchStartTime, boolean isInConsentWindow) {
        return fetchDataTimeNarrower.narrowTransactionFetchStartTime(transactionFetchStartTime, isInConsentWindow);
    }
}

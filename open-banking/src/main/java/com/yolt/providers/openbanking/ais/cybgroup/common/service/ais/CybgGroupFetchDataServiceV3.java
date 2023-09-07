package com.yolt.providers.openbanking.ais.cybgroup.common.service.ais;

import com.yolt.providers.common.ais.DataProviderResponse;
import com.yolt.providers.common.exception.JsonParseException;
import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.exception.ProviderFetchDataException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.cybgroup.common.model.CybgGroupAccessMeansV2;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.transaction.DefaultTransactionMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import lombok.SneakyThrows;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import nl.ing.lovebird.providershared.DataProviderResponseFailedAccount;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CybgGroupFetchDataServiceV3 extends DefaultFetchDataService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    private final RestClient restClient;
    private final Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountsSupplier;
    private final DefaultProperties properties;

    public CybgGroupFetchDataServiceV3(final RestClient restClient,
                                       final DefaultProperties properties,
                                       final DefaultTransactionMapper transactionMapper,
                                       final DefaultAccountMapper accountMapper,
                                       Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                       final Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                       final UnaryOperator<List<OBAccount6>> accountFilter,
                                       final Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                       final Duration consentWindow,
                                       final String endpointVersion,
                                       final Clock clock) {
        super(restClient,
                properties,
                transactionMapper,
                directDebitMapper,
                standingOrderMapper,
                accountMapper,
                accountFilter,
                supportedAccountSupplier,
                consentWindow,
                endpointVersion,
                clock);
        this.restClient = restClient;
        this.supportedAccountsSupplier = supportedAccountSupplier;
        this.properties = properties;
    }

    public List<OBAccount6> getAccounts(final HttpClient httpClient,
                                        final DefaultAuthMeans authenticationMeans,
                                        final AccessMeans accessToken) throws TokenInvalidException {
        final List<OBAccount6> responseAccounts = new ArrayList<>();

        String currentPage = getAccountsUrl();
        String nextPage;
        int pageCounter = 1;

        do {
            try {
                OBReadAccount6 accountGETResponse = restClient.fetchAccounts(httpClient, getAdjustedUrlPath(currentPage), accessToken,
                        authenticationMeans.getInstitutionId(), OBReadAccount6.class);
                nextPage = accountGETResponse.getLinks().getNext();

                for (OBAccount6 account : accountGETResponse.getData().getAccount()) {

                    if (supportedAccountsSupplier.get().contains(account.getAccountSubType())) {
                        responseAccounts.add(account);
                    }
                }
            } catch (RestClientException e) {
                throw new TokenInvalidException("Error when retrieving account during creating access means");
            }
            // Prevent infinite loop on failure to get nextPage
            // Failed Account will already be added because an exception will be thrown and caught during 'performRequest()' call.
            if (Objects.equals(currentPage, nextPage)) {
                break;
            }
            currentPage = nextPage;
            pageCounter++;
        } while (!StringUtils.isEmpty(nextPage) && pageCounter <= properties.getPaginationLimit());

        return responseAccounts;
    }

    @Override
    public DataProviderResponse getAccountsAndTransactions(final HttpClient httpClient,
                                                           final DefaultAuthMeans authenticationMeans,
                                                           final Instant transactionsFetchStartTime,
                                                           final AccessMeans accessMeans) throws TokenInvalidException, ProviderFetchDataException {
        if (!(accessMeans instanceof CybgGroupAccessMeansV2)) {
            throw new TokenInvalidException("Error while parsing access means");
        }

        List<ProviderAccountDTO> responseAccounts = new ArrayList<>();

        try {
            for (OBAccount6 accountDTO : ((CybgGroupAccessMeansV2) accessMeans).getCachedAccounts()) {
                checkAndProcessAccount(httpClient,
                        authenticationMeans,
                        transactionsFetchStartTime,
                        accessMeans,
                        responseAccounts,
                        accountDTO);
            }
        } catch (JsonParseException | RestClientException | MissingDataException e) {
            throw new ProviderFetchDataException(e);
        }
        return new DataProviderResponse(Collections.unmodifiableList(responseAccounts));
    }

    @Override
    protected List<DirectDebitDTO> getDirectDebits(final HttpClient httpClient,
                                                   final AccessMeans accessToken,
                                                   final String externalAccountId,
                                                   final DefaultAuthMeans authenticationMeans,
                                                   final com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code accountSubType) {
        if (OBExternalAccountSubType1Code.SAVINGS.equals(accountSubType)) {
            return Collections.emptyList();
        }
        return super.getDirectDebits(httpClient, accessToken, externalAccountId, authenticationMeans, accountSubType);
    }

    @SneakyThrows
    private DataProviderResponseFailedAccount handleError(Exception e, String externalAccountId) {
        if (e instanceof JsonParseException) {
            JsonParseException exception = (JsonParseException) e;
            return new DataProviderResponseFailedAccount(exception.getJsonResponse(), externalAccountId, exception.getMessage());
        } else if (e instanceof HttpStatusCodeException) {
            HttpStatusCodeException exception = (HttpStatusCodeException) e;
            return new DataProviderResponseFailedAccount(exception.getResponseBodyAsString(), externalAccountId, exception.getMessage());
        } else if (e instanceof RestClientException || e instanceof MissingDataException) {
            return new DataProviderResponseFailedAccount(null, externalAccountId, e.getMessage());
        }
        throw e;
    }

    @Override
    protected String formatFromBookingDateTime(final Instant transactionsFetchStartTime) {
        return DATE_TIME_FORMATTER.format(OffsetDateTime
                .ofInstant(transactionsFetchStartTime.truncatedTo(ChronoUnit.SECONDS), ZoneOffset.UTC));
    }
}

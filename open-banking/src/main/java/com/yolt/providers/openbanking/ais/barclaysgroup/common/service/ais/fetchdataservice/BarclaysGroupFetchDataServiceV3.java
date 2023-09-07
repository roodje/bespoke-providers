package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.fetchdataservice;

import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeansState;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapperV3;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderAccountDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class BarclaysGroupFetchDataServiceV3 extends DefaultFetchDataServiceV2 {
    private static final String ACCOUNT_BLOCKED_CODE = "UK.OBIE.Field.Unexpected";
    private static final String ACCOUNT_BLOCKED_MESSAGE = "Accounts request could not be processed due to invalid data.";

    public BarclaysGroupFetchDataServiceV3(RestClient restClient,
                                           PartiesRestClient partiesRestClient,
                                           DefaultProperties properties,
                                           Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                           Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                           Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                           Function<OBParty2, PartyDto> partiesMapper,
                                           DefaultAccountMapperV3 accountMapper, UnaryOperator<List<OBAccount6>> accountFilter,
                                           Supplier<Set<OBExternalAccountSubType1Code>> getSupportedAccountSubtypes,
                                           Duration consentWindow,
                                           String endpointsVersion,
                                           Clock clock) {
        super(restClient, partiesRestClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, partiesMapper, accountMapper,
                accountFilter, getSupportedAccountSubtypes, consentWindow, endpointsVersion, clock);
    }

    @Override
    protected String formatFromBookingDateTime(final Instant transactionsFetchStartTime) {
        return transactionsFetchStartTime.truncatedTo(ChronoUnit.MILLIS).toString();
    }

    @Override
    protected void checkAndProcessAccount(HttpClient httpClient, DefaultAuthMeans authenticationMeans, Instant transactionsFetchStartTime, AccessMeansState accessMeansState, List<ProviderAccountDTO> responseAccounts, OBAccount6 account) throws TokenInvalidException {
        try {
            super.checkAndProcessAccount(httpClient, authenticationMeans, transactionsFetchStartTime, accessMeansState, responseAccounts, account);
        } catch (HttpStatusCodeException e) {
            if (!e.getStatusCode().equals(HttpStatus.BAD_REQUEST) &&
                    !e.getResponseBodyAsString().contains(ACCOUNT_BLOCKED_CODE) &&
                    !e.getResponseBodyAsString().contains(ACCOUNT_BLOCKED_MESSAGE)) {
                throw e;
            }
        }
    }
}

package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.service;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataServiceV3;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class PermanentTsbFetchDataServiceV1 extends DefaultFetchDataServiceV3 {

    private final Clock clock;

    public PermanentTsbFetchDataServiceV1(RestClient restClient,
                                          PartiesRestClient partiesRestClient,
                                          DefaultProperties properties,
                                          Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                          Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                          Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                          Function<OBParty2, PartyDto> partiesMapper,
                                          AccountMapperV2 accountMapper, Function<Instant, String> fromBookingDateTimeFormatter,
                                          UnaryOperator<List<OBAccount6>> accountFilter,
                                          Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                          Duration consentWindow,
                                          String endpointsVersion,
                                          Clock clock) {
        super(restClient, partiesRestClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, partiesMapper, accountMapper, fromBookingDateTimeFormatter, accountFilter,
                supportedAccountSupplier, consentWindow, endpointsVersion, clock);
        this.clock = clock;
    }

    @Override
    protected Instant narrowTransactionFetchStartTimeHook(Instant transactionFetchStartTime, boolean isInConsentWindow) {
        final Instant daysAgo = Instant.now(clock).minus(Duration.ofDays(89L));
        return transactionFetchStartTime.isBefore(daysAgo) ? daysAgo : transactionFetchStartTime;
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

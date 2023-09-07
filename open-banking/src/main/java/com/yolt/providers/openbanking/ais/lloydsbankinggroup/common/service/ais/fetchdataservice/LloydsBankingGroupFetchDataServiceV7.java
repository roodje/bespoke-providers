package com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.service.ais.fetchdataservice;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class LloydsBankingGroupFetchDataServiceV7 extends DefaultFetchDataService {


    public LloydsBankingGroupFetchDataServiceV7(RestClient restClient,
                                                DefaultProperties properties,
                                                Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                                Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                                Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                                DefaultAccountMapper accountMapper, UnaryOperator<List<OBAccount6>> accountFilter,
                                                Supplier<Set<OBExternalAccountSubType1Code>> getSupportedAccountSubtypes,
                                                Duration consentWindow,
                                                String endpointsVersion,
                                                Clock clock) {
        super(restClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, accountMapper, accountFilter, getSupportedAccountSubtypes, consentWindow, endpointsVersion, clock);
    }


    @Override
    protected String formatFromBookingDateTime(final Instant transactionsFetchStartTime) {
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC).truncatedTo(ChronoUnit.MILLIS));
    }
}

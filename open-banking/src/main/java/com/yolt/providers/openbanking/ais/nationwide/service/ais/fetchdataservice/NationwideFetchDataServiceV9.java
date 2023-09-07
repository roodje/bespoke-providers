package com.yolt.providers.openbanking.ais.nationwide.service.ais.fetchdataservice;


import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.supportedaccounts.DefaultSupportedAccountsSupplier;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapperV3;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;


public class NationwideFetchDataServiceV9 extends DefaultFetchDataServiceV2 {


    public NationwideFetchDataServiceV9(RestClient restClient,
                                        PartiesRestClient partiesRestClient,
                                        DefaultProperties properties,
                                        Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                        Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                        Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                        Function<OBParty2, PartyDto> partiesMapper,
                                        DefaultAccountMapperV3 accountMapper,
                                        UnaryOperator<List<OBAccount6>> accountFilter,
                                        String endpointsVersion,
                                        Clock clock) {
        super(restClient, partiesRestClient, properties, transactionMapper, directDebitMapper, standingOrderMapper, partiesMapper, accountMapper, accountFilter,
                new DefaultSupportedAccountsSupplier(), Duration.ofMinutes(5), endpointsVersion, clock);
    }


    @Override
    protected String formatFromBookingDateTime(final Instant transactionsFetchStartTime) {
        return DateTimeFormatter
                .ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                .format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC));
    }
}

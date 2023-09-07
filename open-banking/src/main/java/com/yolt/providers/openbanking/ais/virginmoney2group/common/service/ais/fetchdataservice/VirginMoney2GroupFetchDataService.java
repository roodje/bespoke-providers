package com.yolt.providers.openbanking.ais.virginmoney2group.common.service.ais.fetchdataservice;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.dto.PartyDto;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataServiceV2;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapperV2;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.PartiesRestClient;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class VirginMoney2GroupFetchDataService extends DefaultFetchDataServiceV2 {

    private static final String GET_ACCOUNT_PARTIES_URL = "/aisp/accounts/%s/party";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx");

    public VirginMoney2GroupFetchDataService(RestClient restClient,
                                             PartiesRestClient partiesRestClient,
                                             DefaultProperties properties,
                                             Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                             Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                             Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                             Function<OBParty2, PartyDto> partiesMapper,
                                             AccountMapperV2 accountMapper,
                                             UnaryOperator<List<OBAccount6>> accountFilter,
                                             Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                             Duration consentWindow,
                                             String endpointsVersion,
                                             Clock clock) {
        super(restClient,
                partiesRestClient,
                properties,
                transactionMapper,
                directDebitMapper,
                standingOrderMapper,
                partiesMapper,
                accountMapper,
                accountFilter,
                supportedAccountSupplier,
                consentWindow,
                endpointsVersion,
                clock);
    }

    @Override
    protected String getAccountPartiesUrlTemplate() {
        return GET_ACCOUNT_PARTIES_URL;
    }

    @Override
    protected String formatFromBookingDateTime(Instant transactionsFetchStartTime) {
        return UriUtils.encode(DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC)), StandardCharsets.UTF_8);
    }
}

package com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.ais.fetchdataservice;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.domain.AccessMeans;
import com.yolt.providers.openbanking.ais.generic2.http.HttpClient;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class BankOfIrelandGroupFetchDataServiceV6 extends DefaultFetchDataService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    public BankOfIrelandGroupFetchDataServiceV6(RestClient restClient,
                                                DefaultProperties properties,
                                                Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                                Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                                Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                                DefaultAccountMapper accountMapper, UnaryOperator<List<OBAccount6>> accountFilter,
                                                Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                                Duration consentWindow,
                                                String endpointsVersion,
                                                Clock clock) {
        super(restClient, properties, transactionMapper, directDebitMapper, standingOrderMapper,
                accountMapper, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock);
    }

    @Override
    protected List<DirectDebitDTO> getDirectDebits(HttpClient httpClient,
                                                   AccessMeans accessToken,
                                                   String externalAccountId,
                                                   DefaultAuthMeans authenticationMeans,
                                                   OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }

    @Override
    protected String formatFromBookingDateTime(final Instant transactionsFetchStartTime) {
        return DATE_TIME_FORMATTER.format(OffsetDateTime.ofInstant(transactionsFetchStartTime, ZoneOffset.UTC));
    }
}

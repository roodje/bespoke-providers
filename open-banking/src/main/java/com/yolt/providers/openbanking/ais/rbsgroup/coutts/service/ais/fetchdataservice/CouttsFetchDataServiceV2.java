package com.yolt.providers.openbanking.ais.rbsgroup.coutts.service.ais.fetchdataservice;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.DefaultAccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class CouttsFetchDataServiceV2 extends DefaultFetchDataService {

    private final UrlAdapter urlAdapter;

    public CouttsFetchDataServiceV2(RestClient restClient,
                                    DefaultProperties properties,
                                    Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                    Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                    Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                    DefaultAccountMapper accountMapper,
                                    UnaryOperator<List<OBAccount6>> accountFilter,
                                    Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                    Duration consentWindow,
                                    String endpointsVersion,
                                    UrlAdapter urlAdapter,
                                    Clock clock) {
        super(restClient, properties, transactionMapper, directDebitMapper, standingOrderMapper,
                accountMapper, accountFilter, supportedAccountSupplier, consentWindow, endpointsVersion, clock);
        this.urlAdapter = urlAdapter;
    }

    @Override
    protected String adaptNextPageHook(String url) {
        return urlAdapter.removePortNumberFromUrl(url);
    }
}

package com.yolt.providers.openbanking.ais.newdaygroup.common.service;

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

import java.time.Clock;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NewDayGroupFetchDataServiceV2 extends DefaultFetchDataService {

    private static final String GET_TRANSACTIONS_URL_TEMPLATES = "/aisp/accounts/%s/transactions?fromStatementDateTime=%s";

    public NewDayGroupFetchDataServiceV2(final RestClient restClient,
                                         final DefaultProperties properties,
                                         final Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                         final Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                         final Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                         final DefaultAccountMapper accountMapper,
                                         final UnaryOperator<List<OBAccount6>> accountFilter,
                                         final Supplier<Set<OBExternalAccountSubType1Code>> supportedAccountSupplier,
                                         final Duration consentWindow,
                                         final String endpointsVersion,
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
                endpointsVersion,
                clock);
    }

    // DOC SAYS IT'S NOT IMPLEMENTED AT THEIR SIDE
    @Override
    protected List<DirectDebitDTO> getDirectDebits(final HttpClient httpClient,
                                                   final AccessMeans accessToken,
                                                   final String externalAccountId,
                                                   final DefaultAuthMeans authenticationMeans,
                                                   final OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }

    // DOC SAYS IT'S NOT IMPLEMENTED AT THEIR SIDE
    @Override
    protected List<StandingOrderDTO> getStandingOrders(final HttpClient httpClient,
                                                       final AccessMeans accessToken,
                                                       final String externalAccountId,
                                                       final DefaultAuthMeans authenticationMeans,
                                                       final OBExternalAccountSubType1Code accountSubType) {
        return Collections.emptyList();
    }

    @Override
    protected String getTransactionsUrlTemplates() {
        return GET_TRANSACTIONS_URL_TEMPLATES;
    }

}

package com.yolt.providers.openbanking.ais.vanquisgroup.common.service.ais.fetchdataservice;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import com.yolt.providers.openbanking.ais.generic2.service.ais.fetchdataservice.DefaultFetchDataService;
import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.account.AccountMapper;
import com.yolt.providers.openbanking.ais.generic2.service.restclient.RestClient;
import com.yolt.providers.openbanking.dto.ais.openbanking316.*;
import nl.ing.lovebird.providerdomain.DirectDebitDTO;
import nl.ing.lovebird.providerdomain.ProviderTransactionDTO;
import nl.ing.lovebird.providerdomain.StandingOrderDTO;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class VanquisGroupFetchDataServiceV3 extends DefaultFetchDataService {

    private final Clock clock;

    public VanquisGroupFetchDataServiceV3(final RestClient restClient,
                                          final DefaultProperties properties,
                                          final Function<OBTransaction6, ProviderTransactionDTO> transactionMapper,
                                          final Function<OBReadDirectDebit2DataDirectDebit, Optional<DirectDebitDTO>> directDebitMapper,
                                          final Function<OBStandingOrder6, StandingOrderDTO> standingOrderMapper,
                                          final AccountMapper accountMapper,
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
        this.clock = clock;
    }

    /**
     * Vanquis Bank introduced a new requirement - 'dateFrom' in transactions API should be not older than 6 months.
     * If we provide 'dateFrom' that is older, we will get HTTP 400.
     *
     * @param transactionFetchStartTime - initial transaction fetch start time that we get from external service
     * @return - transaction fetch start time narrowed down to 6 months if exceeded
     */
    @Override
    protected Instant narrowTransactionFetchStartTimeHook(final Instant transactionFetchStartTime, final boolean isInConsentWindow) {
        final Instant sixMonthsAgo = Instant.now(clock).minus(Period.ofDays(180));
        if (transactionFetchStartTime.isBefore(sixMonthsAgo)) {
            return sixMonthsAgo;
        }
        return transactionFetchStartTime;
    }
}
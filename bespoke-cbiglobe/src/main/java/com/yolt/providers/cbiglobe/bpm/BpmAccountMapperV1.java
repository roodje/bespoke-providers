package com.yolt.providers.cbiglobe.bpm;

import com.yolt.providers.cbiglobe.common.mapper.*;
import nl.ing.lovebird.extendeddata.account.BalanceType;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

import static nl.ing.lovebird.extendeddata.account.BalanceType.*;

public class BpmAccountMapperV1 extends CbiGlobeAccountMapperV2 {

    public BpmAccountMapperV1(CbiGlobeBalanceMapper balanceMapper,
                              CbiGlobeTransactionMapper transactionMapper,
                              CbiGlobeExtendedAccountMapper extendedAcountMapper,
                              CurrencyCodeMapper currencyMapper,
                              Clock clock) {
        super(balanceMapper, transactionMapper, extendedAcountMapper, currencyMapper, clock);
    }

    /**
     * The priority is maintained according to the elements added.
     * The first element has the highest priority
     */
    @Override
    protected List<BalanceType> getCurrentPreferredBalanceTypes() {
        return Arrays.asList(INTERIM_AVAILABLE, INTERIM_BOOKED, CLOSING_BOOKED, OPENING_BOOKED, EXPECTED);
    }

    /**
     * The priority is maintained according to the elements added.
     * The first element has the highest priority
     */
    @Override
    protected List<BalanceType> getAvailablePreferredBalanceTypes() {
        return Arrays.asList(AUTHORISED, EXPECTED, AVAILABLE, FORWARD_AVAILABLE);
    }
}

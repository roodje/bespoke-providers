package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balancetype;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import nl.ing.lovebird.extendeddata.account.BalanceType;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;

public class DefaultBalanceTypeMapper implements Function<OBBalanceType1Code, BalanceType> {

    private static final Map<OBBalanceType1Code, BalanceType> BALANCE_MAPPINGS = new EnumMap<>(OBBalanceType1Code.class);

    static {
        BALANCE_MAPPINGS.put(CLOSINGAVAILABLE, BalanceType.CLOSING_AVAILABLE);
        BALANCE_MAPPINGS.put(CLOSINGBOOKED, BalanceType.CLOSING_BOOKED);
        BALANCE_MAPPINGS.put(CLOSINGCLEARED, BalanceType.CLOSING_CLEARED);
        BALANCE_MAPPINGS.put(EXPECTED, BalanceType.EXPECTED);
        BALANCE_MAPPINGS.put(FORWARDAVAILABLE, BalanceType.FORWARD_AVAILABLE);
        BALANCE_MAPPINGS.put(INFORMATION, BalanceType.INFORMATION);
        BALANCE_MAPPINGS.put(INTERIMAVAILABLE, BalanceType.INTERIM_AVAILABLE);
        BALANCE_MAPPINGS.put(INTERIMBOOKED, BalanceType.INTERIM_BOOKED);
        BALANCE_MAPPINGS.put(INTERIMCLEARED, BalanceType.INTERIM_CLEARED);
        BALANCE_MAPPINGS.put(OPENINGAVAILABLE, BalanceType.OPENING_AVAILABLE);
        BALANCE_MAPPINGS.put(OPENINGBOOKED, BalanceType.OPENING_BOOKED);
        BALANCE_MAPPINGS.put(OPENINGCLEARED, BalanceType.OPENING_CLEARED);
        BALANCE_MAPPINGS.put(PREVIOUSLYCLOSEDBOOKED, BalanceType.PREVIOUSLY_CLOSED_BOOKED);
    }

    @Override
    public BalanceType apply(OBBalanceType1Code balanceCode) {
        return BALANCE_MAPPINGS.get(balanceCode);
    }
}

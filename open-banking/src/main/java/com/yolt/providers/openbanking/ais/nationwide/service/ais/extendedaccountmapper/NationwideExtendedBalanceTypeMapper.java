package com.yolt.providers.openbanking.ais.nationwide.service.ais.extendedaccountmapper;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import nl.ing.lovebird.extendeddata.account.BalanceType;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code.*;

public class NationwideExtendedBalanceTypeMapper implements Function<OBBalanceType1Code, BalanceType> {

    private static final Map<OBBalanceType1Code, BalanceType> BALANCE_MAPPINGS = new EnumMap<>(OBBalanceType1Code.class);

    static {
        // Since we get for credit card accounts (CLOSINGAVAILABLE xor INTERIMCLEARD) and INTERIMAVAILABLE
        // and for current accounts INTERIMBOOKED and INTERIMAVAILABLE - no collision is possible
        BALANCE_MAPPINGS.put(CLOSINGAVAILABLE, BalanceType.AVAILABLE);
        BALANCE_MAPPINGS.put(INTERIMCLEARED, BalanceType.INTERIM_BOOKED);
        BALANCE_MAPPINGS.put(INTERIMAVAILABLE, BalanceType.INTERIM_AVAILABLE);
        BALANCE_MAPPINGS.put(INTERIMBOOKED, BalanceType.INTERIM_BOOKED);
    }

    @Override
    public BalanceType apply(OBBalanceType1Code balanceCode) {
        return BALANCE_MAPPINGS.get(balanceCode);
    }
}

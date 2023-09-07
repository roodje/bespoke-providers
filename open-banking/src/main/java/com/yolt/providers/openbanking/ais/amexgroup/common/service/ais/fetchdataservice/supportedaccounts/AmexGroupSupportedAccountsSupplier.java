package com.yolt.providers.openbanking.ais.amexgroup.common.service.ais.fetchdataservice.supportedaccounts;

import com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBExternalAccountSubType1Code.*;

public class AmexGroupSupportedAccountsSupplier implements Supplier<Set<OBExternalAccountSubType1Code>> {
    protected static final Set<OBExternalAccountSubType1Code> SUPPORTED_ACCOUNT_SUBTYPES = new HashSet<>(
            Arrays.asList(CURRENTACCOUNT, SAVINGS, CREDITCARD, CHARGECARD));

    @Override
    public Set<OBExternalAccountSubType1Code> get() {
        return SUPPORTED_ACCOUNT_SUBTYPES;
    }
}

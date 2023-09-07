package com.yolt.providers.stet.cmarkeagroup.common.mapper.account;

import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;

import java.util.Arrays;
import java.util.List;

public class CmArkeaGroupAccountMapper extends DefaultAccountMapper {
    public CmArkeaGroupAccountMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return Arrays.asList(StetBalanceType.XPCD);
    }

    @Override
    public List<StetBalanceType> getPreferredAvailableBalanceTypes() {
        return Arrays.asList(StetBalanceType.CLBD);
    }
}

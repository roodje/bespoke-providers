package com.yolt.providers.stet.bpcegroup.common.mapper.account;

import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;

import java.util.List;

public class BpceGroupAccountMapper extends DefaultAccountMapper {

    public BpceGroupAccountMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return List.of(StetBalanceType.VALU, StetBalanceType.CLBD, StetBalanceType.XPCD, StetBalanceType.OTHR);
    }

    @Override
    public List<StetBalanceType> getPreferredAvailableBalanceTypes() {
        return List.of(StetBalanceType.CLBD, StetBalanceType.VALU, StetBalanceType.XPCD, StetBalanceType.OTHR);
    }
}
package com.yolt.providers.stet.boursoramagroup.common.mapper.account;

import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;

import java.util.Collections;
import java.util.List;

public class BoursoramaGroupAccountMapper extends DefaultAccountMapper {

    public BoursoramaGroupAccountMapper(DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return List.of(StetBalanceType.XPCD, StetBalanceType.OTHR);
    }

    @Override
    public List<StetBalanceType> getPreferredAvailableBalanceTypes() {
        return Collections.singletonList(StetBalanceType.XPCD);
    }
}
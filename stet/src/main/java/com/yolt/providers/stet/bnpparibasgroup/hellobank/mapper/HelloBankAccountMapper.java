package com.yolt.providers.stet.bnpparibasgroup.hellobank.mapper;

import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupAccountMapper;
import com.yolt.providers.stet.bnpparibasgroup.common.mapper.fetchdata.BnpParibasGroupBalanceMapper;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;

import java.util.List;

public class HelloBankAccountMapper extends BnpParibasGroupAccountMapper {
    public HelloBankAccountMapper(DateTimeSupplier dateTimeSupplier, BnpParibasGroupBalanceMapper balanceMapper) {
        super(dateTimeSupplier, balanceMapper);
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return List.of(StetBalanceType.CLBD, StetBalanceType.XPCD, StetBalanceType.OTHR);
    }

    @Override
    public List<StetBalanceType> getPreferredAvailableBalanceTypes() {
        return List.of(StetBalanceType.OTHR);
    }
}

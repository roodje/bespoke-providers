package com.yolt.providers.axabanque.comdirect.mapper;

import com.yolt.providers.axabanque.common.fetchdata.mapper.AccountTypeMapper;
import com.yolt.providers.axabanque.common.model.external.Account;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.providerdomain.AccountType;
import org.springframework.util.StringUtils;

public class ComdirectAccountTypeMapper implements AccountTypeMapper {
    @Override
    public AccountType mapToAccountType(Account account) {
        if (StringUtils.isEmpty(account.getCashAccountType()) && !StringUtils.isEmpty(account.getMaskedPan())) {
            return AccountType.CREDIT_CARD;
        }
        switch (account.getCashAccountType()) {
            case "CACC":
                return AccountType.CURRENT_ACCOUNT;
            case "SVGS":
                return AccountType.SAVINGS_ACCOUNT;
            default:
                return null;
        }
    }

    @Override
    public ExternalCashAccountType mapToExternalCashAccountType(Account account) {
        return ExternalCashAccountType.fromCode(account.getCashAccountType());
    }
}

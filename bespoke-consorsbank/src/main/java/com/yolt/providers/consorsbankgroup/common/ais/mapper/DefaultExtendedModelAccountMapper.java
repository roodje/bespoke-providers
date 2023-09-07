package com.yolt.providers.consorsbankgroup.common.ais.mapper;

import com.yolt.providers.consorsbankgroup.dto.AccountDetails;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.common.AccountReferenceDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import nl.ing.lovebird.extendeddata.transaction.AccountReferenceType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

public class DefaultExtendedModelAccountMapper {

    public ExtendedAccountDTO mapAccount(final AccountDetails account, final Map<BalanceType, BalanceDTO> balanceMap, final String accountName) {
        return ExtendedAccountDTO.builder()
                .resourceId(account.getResourceId())
                .bic(account.getBic())
                .accountReferences(Collections.singletonList(new AccountReferenceDTO(AccountReferenceType.IBAN, account.getIban())))
                .balances(new ArrayList<>(balanceMap.values()))
                .currency(CurrencyCode.valueOf(account.getCurrency()))
                .name(accountName)
                .build();
    }
}

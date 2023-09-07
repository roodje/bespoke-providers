package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadCardAccountListResponseTypeCardAccounts;
import lombok.RequiredArgsConstructor;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.account.UsageType;
import org.apache.commons.lang3.StringUtils;

@RequiredArgsConstructor
public class CbiGlobeExtendedCardAccountMapperV1 implements CbiGlobeExtendedCardAccountMapper {

    private final CurrencyCodeMapper currencyCodeMapper;

    @Override
    public ExtendedAccountDTO mapExtendedAccountDtoFromCurrentAccount(ReadCardAccountListResponseTypeCardAccounts account) {
        return ExtendedAccountDTO.builder()
                .status(Status.ENABLED)
                .usage(UsageType.PRIVATE)
                .product(account.getProduct())
                .currency(currencyCodeMapper.toCurrencyCode(account.getCurrency()))
                .name(StringUtils.defaultIfEmpty(account.getName(), account.getResourceId()))
                .cashAccountType(ExternalCashAccountType.CURRENT)
                .build();
    }
}

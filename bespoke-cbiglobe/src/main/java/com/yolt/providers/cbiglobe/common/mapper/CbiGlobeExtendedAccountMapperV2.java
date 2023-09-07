package com.yolt.providers.cbiglobe.common.mapper;

import com.yolt.providers.cbiglobe.dto.ReadAccountListResponseTypeAccounts;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.account.ExtendedAccountDTO;
import nl.ing.lovebird.extendeddata.account.ExternalCashAccountType;
import nl.ing.lovebird.extendeddata.account.Status;
import nl.ing.lovebird.extendeddata.account.UsageType;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
public class CbiGlobeExtendedAccountMapperV2 implements CbiGlobeExtendedAccountMapper {
    private final CurrencyCodeMapper currencyCodeMapper;

    @Override
    public ExtendedAccountDTO mapExtendedAccountDtoFromCurrentAccount(ReadAccountListResponseTypeAccounts account) {
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

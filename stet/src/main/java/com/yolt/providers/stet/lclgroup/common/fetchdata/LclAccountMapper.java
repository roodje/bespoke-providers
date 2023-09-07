package com.yolt.providers.stet.lclgroup.common.fetchdata;

import com.yolt.providers.stet.generic.dto.account.StetAccountDTO;
import com.yolt.providers.stet.generic.dto.account.StetAccountUsage;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceDTO;
import com.yolt.providers.stet.generic.dto.balance.StetBalanceType;
import com.yolt.providers.stet.generic.mapper.DateTimeSupplier;
import com.yolt.providers.stet.generic.mapper.account.DefaultAccountMapper;
import nl.ing.lovebird.extendeddata.account.BalanceDTO;
import nl.ing.lovebird.extendeddata.account.BalanceType;
import nl.ing.lovebird.extendeddata.account.UsageType;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yolt.providers.stet.generic.dto.account.StetAccountUsage.PRIV;
import static com.yolt.providers.stet.generic.dto.balance.StetBalanceType.OTHR;

public class LclAccountMapper extends DefaultAccountMapper {

    public LclAccountMapper(final DateTimeSupplier dateTimeSupplier) {
        super(dateTimeSupplier);
    }

    @Override
    protected CurrencyCode mapToCurrencyCode(final StetAccountDTO account, final List<StetBalanceDTO> balances) {
        return account.getCurrency();
    }

    @Override
    public List<StetBalanceType> getPreferredAvailableBalanceTypes() {
        return List.of(StetBalanceType.XPCD, StetBalanceType.CLBD);
    }

    @Override
    public List<StetBalanceType> getPreferredCurrentBalanceTypes() {
        return List.of(StetBalanceType.CLBD, StetBalanceType.XPCD);
    }

    @Override
    protected UsageType mapToUsageType(final StetAccountUsage usage) {
        if (PRIV.equals(usage)) {
            return UsageType.PRIVATE;
        }
        return null;
    }

    @Override
    public List<BalanceDTO> mapToBalanceDTOs(final List<StetBalanceDTO> balances) {
        return balances.stream()
                .filter(balance -> (balance.getType() != null) && (balance.getType() != OTHR))
                .map(balance -> mapToBalanceDTO(balance, mapToBalanceType(balance.getType())))
                .collect(Collectors.toList());
    }

    @Override
    protected BigDecimal getBalanceAmount(final List<StetBalanceDTO> balances, final List<StetBalanceType> preferredBalanceTypes) {
        Map<StetBalanceType, BigDecimal> balanceAmountMap = balances.stream()
                .collect(Collectors.toMap(StetBalanceDTO::getType, StetBalanceDTO::getAmount, (firstValue, secondValue) -> firstValue));

        for (StetBalanceType preferredBalanceType : preferredBalanceTypes) {
            if (balanceAmountMap.containsKey(preferredBalanceType)) {
                return balanceAmountMap.get(preferredBalanceType);
            }
        }
        return null;
    }

    private BalanceDTO mapToBalanceDTO(StetBalanceDTO balance, BalanceType balanceType) {
        return BalanceDTO.builder()
                .balanceAmount(BalanceAmountDTO.builder()
                        .amount(balance.getAmount())
                        .currency(balance.getCurrency())
                        .build())
                .balanceType(balanceType)
                .referenceDate(dateTimeSupplier.convertToZonedDateTime(balance.getReferenceDate()))
                .build();
    }
}

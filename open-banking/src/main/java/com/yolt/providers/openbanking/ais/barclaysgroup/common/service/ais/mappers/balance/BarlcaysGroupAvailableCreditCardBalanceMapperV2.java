package com.yolt.providers.openbanking.ais.barclaysgroup.common.service.ais.mappers.balance;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataCreditLine;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataCreditLine.TypeEnum.AVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataCreditLine.TypeEnum.CREDIT;

@AllArgsConstructor
public class BarlcaysGroupAvailableCreditCardBalanceMapperV2 extends DefaultBalanceMapper {

    @Override
    public BigDecimal getBalance(final Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType,
                                 final Supplier<List<OBBalanceType1Code>> balanceListTypeSupplier) {
        BigDecimal currentBalance = super.getBalance(balancesByType, balanceListTypeSupplier);
        OBReadBalance1DataBalance firstSuitableBalance = getFirstSuitableBalance(balancesByType, balanceListTypeSupplier.get());
        List<OBReadBalance1DataCreditLine> creditLines = firstSuitableBalance == null ? null : firstSuitableBalance.getCreditLine();
        return currentBalance == null || creditLines == null
                ? null : getAvailableBalanceTypeForCreditCardUsingCreditLine(creditLines, currentBalance.negate());
    }

    private OBReadBalance1DataBalance getFirstSuitableBalance(final Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType,
                                                              final List<OBBalanceType1Code> balanceTypes) {
        return balanceTypes.stream()
                .filter(balancesByType::containsKey)
                .findAny().map(balancesByType::get)
                .orElse(null);
    }

    private BigDecimal getAvailableBalanceTypeForCreditCardUsingCreditLine(final List<OBReadBalance1DataCreditLine> creditLines,
                                                                           final BigDecimal currentBalance) {
        for (OBReadBalance1DataCreditLine creditLine : creditLines) {
            if (creditLine.getType() == AVAILABLE) {
                return new BigDecimal(creditLine.getAmount().getAmount());
            }
            if (creditLine.getType() == CREDIT) {
                return new BigDecimal(creditLine.getAmount().getAmount()).subtract(currentBalance);
            }
        }
        return currentBalance.negate();
    }
}
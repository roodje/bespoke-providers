package com.yolt.providers.openbanking.ais.sainsburys.service.ais.mappers;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.DefaultBalanceMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataCreditLine;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataCreditLine.TypeEnum.AVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataCreditLine.TypeEnum.CREDIT;

@RequiredArgsConstructor
public class SainsburysAvailableBalanceForCreditCardMapper extends DefaultBalanceMapper {

    private final Supplier<List<OBBalanceType1Code>> getCurrentBalanceTypeForCreditCard;

    @Override
    public BigDecimal getBalance(Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType,
                                 Supplier<List<OBBalanceType1Code>> balanceListTypeSupplier) {
        BigDecimal currentBalance = super.getBalance(balancesByType, getCurrentBalanceTypeForCreditCard);

        List<OBReadBalance1DataCreditLine> creditLines = balanceListTypeSupplier.get().stream()
                .filter(balancesByType::containsKey)
                .map(obBalanceType1Code -> balancesByType.get(obBalanceType1Code))
                .map(obReadBalance1DataBalance -> obReadBalance1DataBalance.getCreditLine())
                .findFirst()
                .orElse(null);

        return currentBalance == null || creditLines == null ? null
                : getAvailableBalanceTypeForCreditCardUsingCreditLine(creditLines, currentBalance);
    }

    private BigDecimal getAvailableBalanceTypeForCreditCardUsingCreditLine(List<OBReadBalance1DataCreditLine> creditLines,
                                                                           BigDecimal currentBalance) {
        BigDecimal creditLimit = new BigDecimal(0);
        for (OBReadBalance1DataCreditLine creditLine : creditLines) {
            if (creditLine.getType() == AVAILABLE) {
                return new BigDecimal(creditLine.getAmount().getAmount());
            }
            if (creditLine.getType() == CREDIT) {
                creditLimit = new BigDecimal(creditLine.getAmount().getAmount());
            }
        }
        return creditLimit.subtract(currentBalance);
    }
}

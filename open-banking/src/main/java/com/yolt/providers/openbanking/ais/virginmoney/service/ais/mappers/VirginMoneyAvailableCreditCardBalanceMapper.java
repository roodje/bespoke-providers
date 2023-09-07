package com.yolt.providers.openbanking.ais.virginmoney.service.ais.mappers;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBBalanceType1Code;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataCreditLine;
import lombok.AllArgsConstructor;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataCreditLine.TypeEnum.AVAILABLE;
import static com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataCreditLine.TypeEnum.CREDIT;

@AllArgsConstructor
public class VirginMoneyAvailableCreditCardBalanceMapper implements BalanceMapper {
    private final BalanceMapper currentCreditCardBalanceMapper;
    private final Supplier<List<OBBalanceType1Code>> currentBalanceTypeForCreditCardSupplier;

    @Override
    public BigDecimal getBalance(Map<OBBalanceType1Code, OBReadBalance1DataBalance> balancesByType, Supplier<List<OBBalanceType1Code>> balanceTypeSupplier) {
        BigDecimal currentBalance = currentCreditCardBalanceMapper.getBalance(balancesByType, currentBalanceTypeForCreditCardSupplier);
        OBReadBalance1DataBalance balance = balanceTypeSupplier.get().stream()
                .filter(balancesByType::containsKey)
                .findFirst()
                .map(balancesByType::get)
                .orElse(null);

        List<OBReadBalance1DataCreditLine> creditLines = balance.getCreditLine();
        BigDecimal availableBalance = null;
        if (currentBalance != null && creditLines != null) {
            currentBalance = currentBalance.negate();
            availableBalance = getAvailableBalanceTypeForCreditCardUsingCreditLine(creditLines, currentBalance);
        }
        return availableBalance;
    }

    private BigDecimal getAvailableBalanceTypeForCreditCardUsingCreditLine(final List<OBReadBalance1DataCreditLine> creditLines,
                                                                           final BigDecimal currentBalance) {
        BigDecimal creditLimit = BigDecimal.ZERO;
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

    @Override
    public BigDecimal getBalance(String amount, boolean shouldNegate) {
        if (ObjectUtils.isEmpty(amount)) {
            return null;
        }
        BigDecimal value = new BigDecimal(amount);
        return shouldNegate ? value.negate() : value;
    }
}

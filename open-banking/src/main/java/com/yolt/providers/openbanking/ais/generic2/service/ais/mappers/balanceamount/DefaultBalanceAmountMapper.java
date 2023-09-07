package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balanceamount;

import com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.balance.BalanceMapper;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode1;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBCreditDebitCode2;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBReadBalance1DataBalance;
import com.yolt.providers.openbanking.dto.ais.openbanking316.OBTransaction6;
import lombok.AllArgsConstructor;
import nl.ing.lovebird.extendeddata.common.BalanceAmountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

import java.util.function.Function;

@AllArgsConstructor
public class DefaultBalanceAmountMapper implements BalanceAmountMapper {
    private final Function<String, CurrencyCode> currencyMapper;
    private final BalanceMapper balanceMapper;

    @Override
    public BalanceAmountDTO apply(OBTransaction6 transaction) {
        return getBalanceAmountDTO(transaction.getAmount().getCurrency(), transaction.getAmount().getAmount(), !OBCreditDebitCode1.CREDIT.equals(transaction.getCreditDebitIndicator()));
    }

    @Override
    public BalanceAmountDTO apply(OBReadBalance1DataBalance balance) {
        return getBalanceAmountDTO(balance.getAmount().getCurrency(), balance.getAmount().getAmount(), OBCreditDebitCode2.DEBIT.equals(balance.getCreditDebitIndicator()));
    }

    private BalanceAmountDTO getBalanceAmountDTO(String currency, String amount, boolean shouldNegate) {
        return new BalanceAmountDTO(
                currencyMapper.apply(currency),
                balanceMapper.getBalance(amount, shouldNegate));
    }
}

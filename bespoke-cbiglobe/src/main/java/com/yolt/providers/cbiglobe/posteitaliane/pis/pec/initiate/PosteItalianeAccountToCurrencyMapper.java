package com.yolt.providers.cbiglobe.posteitaliane.pis.pec.initiate;

import com.yolt.providers.cbiglobe.common.exception.PaymentFailedException;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.CbiGlobeAccountToCurrencyMapper;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

public class PosteItalianeAccountToCurrencyMapper implements CbiGlobeAccountToCurrencyMapper {

    @Override
    public String map(SepaAccountDTO sepaAccountDTO) {
        return vetoIfCreditorDebtorCurrencyNotValid(
                CurrencyCode.valueOf(sepaAccountDTO.getCurrency().name()));
    }

    private String vetoIfCreditorDebtorCurrencyNotValid(CurrencyCode currency) {
        if (currency.equals(CurrencyCode.EUR)) {
            return currency.name();
        } else {
            throw new PaymentFailedException("Currency used in payment request for Creditor or Debtor field is not allowed: " + currency);
        }
    }
}

package com.yolt.providers.cbiglobe.posteitaliane.pis.pec.initiate;

import com.yolt.providers.cbiglobe.common.exception.PaymentFailedException;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.CbiGlobeInstructedAmountToCurrencyMapper;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

public class PosteItalianeInstructedAmountToCurrencyMapper implements CbiGlobeInstructedAmountToCurrencyMapper {

    @Override
    public String map(SepaInitiatePaymentRequestDTO request) {
        return vetoIfInstructedAmountCurrencyNotValid(
                request.getCreditorAccount().getCurrency());
    }

    private String vetoIfInstructedAmountCurrencyNotValid(CurrencyCode currency) {
        return switch (currency) {
            case EUR, USD, AUD, CAD, CHF, GBP, JPY -> currency.name();
            default -> throw new PaymentFailedException("Currency used in payment request Instructed field is not allowed: " + currency);
        };
    }
}

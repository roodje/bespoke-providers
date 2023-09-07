package com.yolt.providers.openbanking.ais.generic2.service.ais.mappers.creditcard;

import nl.ing.lovebird.providerdomain.ProviderCreditCardDTO;

import java.math.BigDecimal;
import java.util.function.Function;

public class CreditCardMapper implements Function<BigDecimal, ProviderCreditCardDTO> {

    @Override
    public ProviderCreditCardDTO apply(BigDecimal availableBalance) {
        return ProviderCreditCardDTO.builder()
                .availableCreditAmount(availableBalance)
                .build();
    }
}

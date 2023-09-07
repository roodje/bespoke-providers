package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;

public class CbiGlobeDefaultInstructedAmountToCurrencyMapper implements CbiGlobeInstructedAmountToCurrencyMapper {

    @Override
    public String map(SepaInitiatePaymentRequestDTO request) {
        return CurrencyCode.EUR.name();
    }
}

package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;

public interface CbiGlobeInstructedAmountToCurrencyMapper {
    String map(SepaInitiatePaymentRequestDTO request);
}

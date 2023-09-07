package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.yolt.providers.common.pis.common.PaymentType;
import lombok.Value;

import java.util.UUID;

@Value
public class YoltBankUkSubmitPreExecutionResult {

    UUID paymentId;
    UUID clientId;
    PaymentType paymentType;
}

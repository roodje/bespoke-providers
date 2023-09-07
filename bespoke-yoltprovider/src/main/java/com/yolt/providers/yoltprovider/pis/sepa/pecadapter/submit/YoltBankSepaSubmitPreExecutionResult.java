package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import lombok.Value;

@Value
public class YoltBankSepaSubmitPreExecutionResult {

    String paymentId;
    PaymentAuthenticationMeans authenticationMeans;
    Signer signer;
    PaymentType paymentType;

}

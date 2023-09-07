package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import lombok.Value;

@Value
public class YoltBankSepaInitiatePaymentPreExecutionResult {

    SepaInitiatePaymentRequestDTO requestDTO;
    PaymentAuthenticationMeans authenticationMeans;
    Signer signer;
    String baseClientRedirectUrl;
    String state;
}

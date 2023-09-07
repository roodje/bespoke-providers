package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPeriodicPaymentRequestDTO;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import lombok.Value;

@Value
public class YoltBankUkInitiatePeriodicPaymentPreExecutionResult {

    InitiateUkDomesticPeriodicPaymentRequestDTO requestDTO;
    PaymentAuthenticationMeans authenticationMeans;
    Signer signer;
    String baseClientRedirectUrl;
    String state;
    String consentId;
    String consentUri;
}

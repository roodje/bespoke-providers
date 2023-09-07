package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.common.cryptography.Signer;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.yoltprovider.pis.PaymentAuthenticationMeans;
import lombok.Value;

@Value
public class YoltBankUkInitiateScheduledPaymentPreExecutionResult {

    InitiateUkDomesticScheduledPaymentRequestDTO requestDTO;
    PaymentAuthenticationMeans authenticationMeans;
    Signer signer;
    String baseClientRedirectUrl;
    String state;
    String consentId;
    String consentUri;

}

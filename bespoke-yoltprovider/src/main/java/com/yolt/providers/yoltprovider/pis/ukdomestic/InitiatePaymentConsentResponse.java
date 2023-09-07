package com.yolt.providers.yoltprovider.pis.ukdomestic;

import lombok.Value;

@Value
public class InitiatePaymentConsentResponse {
    String consentUri;
    String paymentConsent;
}

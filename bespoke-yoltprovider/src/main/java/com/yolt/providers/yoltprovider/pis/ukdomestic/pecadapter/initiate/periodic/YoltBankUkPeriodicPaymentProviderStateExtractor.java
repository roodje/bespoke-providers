package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.periodic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;
import com.yolt.providers.yoltprovider.pis.ukdomestic.models.OBWriteDomesticStandingOrderConsentResponse1;

public class YoltBankUkPeriodicPaymentProviderStateExtractor implements UkPaymentProviderStateExtractor<InitiatePaymentConsentResponse, YoltBankUkInitiatePeriodicPaymentPreExecutionResult> {

    private final ObjectMapper objectMapper;

    public YoltBankUkPeriodicPaymentProviderStateExtractor(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public UkProviderState extractUkProviderState(InitiatePaymentConsentResponse initiatePaymentConsentResponse, YoltBankUkInitiatePeriodicPaymentPreExecutionResult result) {
        try {
            OBWriteDomesticStandingOrderConsentResponse1 consentResponse = objectMapper.readValue(initiatePaymentConsentResponse.getPaymentConsent(), OBWriteDomesticStandingOrderConsentResponse1.class);
            return new UkProviderState(consentResponse.getData().getConsentId(), PaymentType.PERIODIC, null);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}

package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single.scheduled;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;

public class YoltBankUkScheduledPaymentProviderStateExtractor implements UkPaymentProviderStateExtractor<InitiatePaymentConsentResponse, YoltBankUkInitiateScheduledPaymentPreExecutionResult> {

    @Override
    public UkProviderState extractUkProviderState(InitiatePaymentConsentResponse initiatePaymentConsentResponse, YoltBankUkInitiateScheduledPaymentPreExecutionResult result) {
        return new UkProviderState(null, PaymentType.SCHEDULED, null);
    }
}

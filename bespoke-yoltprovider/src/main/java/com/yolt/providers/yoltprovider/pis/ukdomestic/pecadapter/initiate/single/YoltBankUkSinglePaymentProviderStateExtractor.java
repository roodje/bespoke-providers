package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.initiate.single;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.yoltprovider.pis.ukdomestic.InitiatePaymentConsentResponse;

public class YoltBankUkSinglePaymentProviderStateExtractor implements UkPaymentProviderStateExtractor<InitiatePaymentConsentResponse, YoltBankUkInitiateSinglePaymentPreExecutionResult> {

    @Override
    public UkProviderState extractUkProviderState(InitiatePaymentConsentResponse initiatePaymentConsentResponse, YoltBankUkInitiateSinglePaymentPreExecutionResult result) {
        return new UkProviderState(null, PaymentType.SINGLE, null);
    }
}

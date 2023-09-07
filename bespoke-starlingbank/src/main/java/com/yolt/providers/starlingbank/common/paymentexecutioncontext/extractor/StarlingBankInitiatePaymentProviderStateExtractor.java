package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.uk.initiate.UkPaymentProviderStateExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.exception.PaymentExecutionTechnicalException;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankInitiatePaymentExecutionContextPreExecutionResult;

public class StarlingBankInitiatePaymentProviderStateExtractor implements UkPaymentProviderStateExtractor<String, StarlingBankInitiatePaymentExecutionContextPreExecutionResult> {

    @Override
    public UkProviderState extractUkProviderState(String httpResponseBody, StarlingBankInitiatePaymentExecutionContextPreExecutionResult preExecutionResult) throws PaymentExecutionTechnicalException {
        return new UkProviderState(
                null,
                PaymentType.SINGLE,
                preExecutionResult.getProviderStatePayload().getPaymentRequest());
    }
}

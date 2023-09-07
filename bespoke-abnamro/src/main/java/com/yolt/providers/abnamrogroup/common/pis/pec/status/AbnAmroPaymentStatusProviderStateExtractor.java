package com.yolt.providers.abnamrogroup.common.pis.pec.status;

import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import com.yolt.providers.abnamrogroup.common.pis.pec.AbnAmroProviderStateSerializer;
import com.yolt.providers.common.pis.paymentexecutioncontext.adapter.sepa.initiate.SepaPaymentProviderStateExtractor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AbnAmroPaymentStatusProviderStateExtractor implements SepaPaymentProviderStateExtractor<TransactionStatusResponse, AbnAmroPaymentStatusPreExecutionResult> {

    private final AbnAmroProviderStateSerializer providerStateSerializer;

    @Override
    public String extractProviderState(TransactionStatusResponse transactionStatusResponse, AbnAmroPaymentStatusPreExecutionResult preExecutionResult) {
        return providerStateSerializer.serialize(preExecutionResult.getProviderState());
    }
}

package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.common;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.YoltBankSepaPaymentStatusesMapper;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate.SepaInitiatePaymentResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YoltBankSepaInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<SepaInitiatePaymentResponse, YoltBankSepaInitiatePaymentPreExecutionResult> {

    private final YoltBankSepaPaymentStatusesMapper sepaPaymentStatusesMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(final SepaInitiatePaymentResponse responseBody, final YoltBankSepaInitiatePaymentPreExecutionResult preExecutionResult) {

        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(responseBody.getStatus().name()),
                sepaPaymentStatusesMapper.mapToInternalPaymentStatus(responseBody.getStatus())
        );
    }
}

package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.starlingbank.common.model.PaymentDetailsResponse;
import com.yolt.providers.starlingbank.common.model.PaymentStatusDetails;
import com.yolt.providers.starlingbank.common.model.PaymentStatusResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.mapper.StarlingBankCommonPaymentStatusMapper;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StarlingBankStatusPaymentStatusesExtractor implements PaymentStatusesExtractor<PaymentStatusResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult> {

    private final StarlingBankCommonPaymentStatusMapper commonPaymentStatusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(PaymentStatusResponse paymentStatusResponse, StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult starlingBankSubmitPaymentExecutionContextPreExecutionResult) {
        return commonPaymentStatusMapper.mapTransactionStatus(paymentStatusResponse.getPayments()
                .stream()
                .findFirst()
                .map(PaymentDetailsResponse::getPaymentStatusDetails)
                .orElseGet(PaymentStatusDetails::new));
    }
}

package com.yolt.providers.rabobank.pis.pec.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.rabobank.dto.external.StatusResponse;
import com.yolt.providers.rabobank.dto.external.TransactionStatus;
import com.yolt.providers.rabobank.pis.pec.RabobankSepaPaymentStatusesMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabobankSepaSubmitPaymentStatusesExtractor implements PaymentStatusesExtractor<StatusResponse, RabobankSepaSubmitPaymentPreExecutionResult> {

    private final RabobankSepaPaymentStatusesMapper statusesMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(StatusResponse statusResponse, RabobankSepaSubmitPaymentPreExecutionResult rabobankSepaSubmitPaymentPreExecutionResult) {
        TransactionStatus status = statusResponse.getTransactionStatus();
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(status.name(), ""),
                statusesMapper.mapToInternalPaymentStatus(status)
        );
    }
}

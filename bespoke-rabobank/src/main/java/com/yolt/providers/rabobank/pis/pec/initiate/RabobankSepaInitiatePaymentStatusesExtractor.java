package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.rabobank.dto.external.InitiatedTransactionResponse;
import com.yolt.providers.rabobank.dto.external.TransactionStatus;
import com.yolt.providers.rabobank.pis.pec.RabobankSepaPaymentStatusesMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RabobankSepaInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<InitiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult> {

    private final RabobankSepaPaymentStatusesMapper statusesMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(InitiatedTransactionResponse initiatedTransactionResponse, RabobankSepaInitiatePreExecutionResult rabobankSepaInitiatedPreExecutionResult) {
        TransactionStatus status = initiatedTransactionResponse.getTransactionStatus();
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(status.name(), ""),
                statusesMapper.mapToInternalPaymentStatus(status)
        );
    }
}

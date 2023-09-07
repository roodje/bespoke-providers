package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.common.pis.sepa.SepaPaymentStatusResponseDTO;
import com.yolt.providers.yoltprovider.pis.sepa.pecadapter.YoltBankSepaPaymentStatusesMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YoltBankSepaSubmitPaymentStatusesExtractor implements PaymentStatusesExtractor<SepaPaymentStatusResponse, YoltBankSepaSubmitPreExecutionResult> {

    private final YoltBankSepaPaymentStatusesMapper sepaPaymentStatusesMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(SepaPaymentStatusResponse responseBody, YoltBankSepaSubmitPreExecutionResult preExecutionResult) {
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(responseBody.getPaymentStatus().name()),
                sepaPaymentStatusesMapper.mapToInternalPaymentStatus(responseBody.getPaymentStatus())
        );
    }
}

package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.yoltprovider.pis.ukdomestic.UkDomesticPaymentStatusMapper;
import com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit.model.PaymentSubmitResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class YotlBankUkDomesticSubmitPaymentStatusesExtractor implements PaymentStatusesExtractor<PaymentSubmitResponse, YoltBankUkSubmitPreExecutionResult> {

    private final UkDomesticPaymentStatusMapper ukDomesticPaymentStatusMapper;

    @Override
    public PaymentStatuses extractPaymentStatuses(PaymentSubmitResponse responseBody, YoltBankUkSubmitPreExecutionResult preExecutionResult) {
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(responseBody.getData().getStatus()),
                ukDomesticPaymentStatusMapper.mapToInternalPaymentStatus(responseBody.getData().getStatus())
        );
    }

}

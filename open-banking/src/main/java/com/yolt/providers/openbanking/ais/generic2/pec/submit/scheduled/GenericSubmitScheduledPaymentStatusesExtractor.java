package com.yolt.providers.openbanking.ais.generic2.pec.submit.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.submit.single.GenericSubmitPaymentPreExecutionResult;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledResponse5Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class GenericSubmitScheduledPaymentStatusesExtractor implements PaymentStatusesExtractor<OBWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult> {

    private final ResponseStatusMapper<OBWriteDomesticScheduledResponse5Data.StatusEnum> responseStatusMapper;

    @SneakyThrows
    @Override
    public PaymentStatuses extractPaymentStatuses(OBWriteDomesticScheduledResponse5 obWriteDomesticScheduledResponse5, GenericSubmitPaymentPreExecutionResult preExecutionResult) {
        var status = obWriteDomesticScheduledResponse5.getData().getStatus();
        return new PaymentStatuses(RawBankPaymentStatus.forStatus(status.toString(), ""),
                responseStatusMapper.mapToEnhancedPaymentStatus(status));
    }
}

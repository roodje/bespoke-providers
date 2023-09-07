package com.yolt.providers.openbanking.ais.generic2.pec.submit.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticResponse5Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class GenericSubmitPaymentStatusesExtractor implements PaymentStatusesExtractor<OBWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult> {

    private final ResponseStatusMapper<OBWriteDomesticResponse5Data.StatusEnum> responseStatusMapper;

    @SneakyThrows
    @Override
    public PaymentStatuses extractPaymentStatuses(OBWriteDomesticResponse5 obWriteDomesticResponse5, GenericSubmitPaymentPreExecutionResult preExecutionResult) {
        var status = obWriteDomesticResponse5.getData().getStatus();
        return new PaymentStatuses(RawBankPaymentStatus.forStatus(status.toString(), ""),
                responseStatusMapper.mapToEnhancedPaymentStatus(status));
    }
}

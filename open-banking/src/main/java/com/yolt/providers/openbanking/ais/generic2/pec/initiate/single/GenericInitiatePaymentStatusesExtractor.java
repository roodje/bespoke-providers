package com.yolt.providers.openbanking.ais.generic2.pec.initiate.single;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticConsentResponse5Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class GenericInitiatePaymentStatusesExtractor implements PaymentStatusesExtractor<OBWriteDomesticConsentResponse5, GenericInitiatePaymentPreExecutionResult> {

    private final ResponseStatusMapper<OBWriteDomesticConsentResponse5Data.StatusEnum> responseStatusMapper;

    @SneakyThrows
    @Override
    public PaymentStatuses extractPaymentStatuses(OBWriteDomesticConsentResponse5 httpResponseBody, GenericInitiatePaymentPreExecutionResult preExecutionResult) {
        var status = httpResponseBody.getData().getStatus();
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(status.toString(), ""),
                responseStatusMapper.mapToEnhancedPaymentStatus(status)
        );
    }
}

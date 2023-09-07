package com.yolt.providers.openbanking.ais.generic2.pec.initiate.scheduled;

import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.status.ResponseStatusMapper;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomesticScheduledConsentResponse5Data;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class GenericInitiateScheduledPaymentStatusesExtractor implements PaymentStatusesExtractor<OBWriteDomesticScheduledConsentResponse5, GenericInitiateScheduledPaymentPreExecutionResult> {

    private final ResponseStatusMapper<OBWriteDomesticScheduledConsentResponse5Data.StatusEnum> responseStatusMapper;

    @SneakyThrows
    @Override
    public PaymentStatuses extractPaymentStatuses(OBWriteDomesticScheduledConsentResponse5 httpResponseBody, GenericInitiateScheduledPaymentPreExecutionResult preExecutionResult) {
        var status = httpResponseBody.getData().getStatus();
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(status.toString(), ""),
                responseStatusMapper.mapToEnhancedPaymentStatus(status)
        );
    }
}

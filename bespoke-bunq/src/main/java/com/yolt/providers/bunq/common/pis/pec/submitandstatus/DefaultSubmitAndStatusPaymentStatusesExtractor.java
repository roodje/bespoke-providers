package com.yolt.providers.bunq.common.pis.pec.submitandstatus;

import com.yolt.providers.bunq.common.model.PaymentServiceProviderDraftPaymentStatusResponse;
import com.yolt.providers.bunq.common.model.PaymentStatusValue;
import com.yolt.providers.common.pis.paymentexecutioncontext.PaymentStatusesExtractor;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DefaultSubmitAndStatusPaymentStatusesExtractor implements PaymentStatusesExtractor<PaymentServiceProviderDraftPaymentStatusResponse, DefaultSubmitAndStatusPaymentPreExecutionResult> {

    @Override
    public PaymentStatuses extractPaymentStatuses(final PaymentServiceProviderDraftPaymentStatusResponse response, final DefaultSubmitAndStatusPaymentPreExecutionResult preExecutionResult) {
        PaymentStatusValue status = response.getStatus();
        return new PaymentStatuses(
                RawBankPaymentStatus.forStatus(status.name(), ""),
                mapToEnhancedPaymentStatus(status)
        );
    }

    private EnhancedPaymentStatus mapToEnhancedPaymentStatus(PaymentStatusValue paymentStatus) {
        return switch (paymentStatus) {
            case REJECTED, CANCELLED -> EnhancedPaymentStatus.REJECTED;
            case EXPIRED -> EnhancedPaymentStatus.NO_CONSENT_FROM_USER;
            case PENDING -> EnhancedPaymentStatus.ACCEPTED;
            case ACCEPTED, COMPLETED -> EnhancedPaymentStatus.COMPLETED;
        };
    }
}

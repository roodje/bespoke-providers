package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamrogroup.common.pis.pec.exception.AbnAmroConsentCancelledException;
import com.yolt.providers.abnamrogroup.common.pis.pec.exception.AbnAmroUserAccessTokenNotProvidedException;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.PaymentExecutionContextPreExecutionErrorHandler;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.PaymentExecutionContextTechnicalExceptionSupplier;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionResult;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.Instant;

@RequiredArgsConstructor
public class AbnAmroPreExecutionErrorHandler implements PaymentExecutionContextPreExecutionErrorHandler {

    private final Clock clock;
    private final PaymentExecutionContextTechnicalExceptionSupplier paymentExecutionContextTechnicalExceptionSupplier;

    @Override
    public <HttpResponseBody, PreExecutionResult> PaymentExecutionResult<HttpResponseBody, PreExecutionResult> handleError(Exception ex) {
        Instant now = Instant.now(clock);
        if (ex instanceof AbnAmroConsentCancelledException) {
            return new PaymentExecutionResult<>(now,
                    now,
                    "",
                    "",
                    "[]",
                    "[]",
                    new PaymentStatuses(RawBankPaymentStatus.forStatus("access_denied", "User has cancelled the consent process"),
                            EnhancedPaymentStatus.NO_CONSENT_FROM_USER),
                    null,
                    null);
        } else if (ex instanceof AbnAmroUserAccessTokenNotProvidedException) {
            return new PaymentExecutionResult<>(now,
                    now,
                    "",
                    "",
                    "[]",
                    "[]",
                    new PaymentStatuses(RawBankPaymentStatus.unknown(),
                            EnhancedPaymentStatus.UNKNOWN),
                    null,
                    null);
        }

        throw paymentExecutionContextTechnicalExceptionSupplier.createPaymentExecutionTechnicalException(ex);
    }
}

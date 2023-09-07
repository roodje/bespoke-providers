package com.yolt.providers.starlingbank.common.errorhandler;

import com.yolt.providers.common.exception.MissingDataException;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.DefaultPaymentExecutionContextPreExecutionErrorHandler;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.PaymentExecutionContextPreExecutionErrorHandler;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionResult;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.RequiredArgsConstructor;

import java.time.Clock;

@RequiredArgsConstructor
public class StarlingPreExecutionErrorHandler implements PaymentExecutionContextPreExecutionErrorHandler {

    private final DefaultPaymentExecutionContextPreExecutionErrorHandler errorHandler;
    private final Clock clock;

    @Override
    public <HttpResponseBody, PreExecutionResult> PaymentExecutionResult<HttpResponseBody, PreExecutionResult> handleError(Exception ex) {
        if (ex instanceof MissingDataException) {
            //MissingDataException is thrown when there is no authorization code in redirectUrl
            return new PaymentExecutionResult<>(
                    clock.instant(),
                    clock.instant(),
                    "",
                    "",
                    "[]",
                    "[]",
                    new PaymentStatuses(RawBankPaymentStatus.forStatus("REJECTED", ex.getMessage()), EnhancedPaymentStatus.NO_CONSENT_FROM_USER),
                    null,
                    null
            );
        }
        if (ex instanceof StepNotSupportedByBankException) {
            //StepNotSupportedByBankException is thrown when trying to call bank for status for payment which is not submitted yet (no payment created at banks side)
            return new PaymentExecutionResult<>(
                    clock.instant(),
                    clock.instant(),
                    "",
                    "",
                    "[]",
                    "[]",
                    new PaymentStatuses(RawBankPaymentStatus.unknown(ex.getMessage()), EnhancedPaymentStatus.INITIATION_SUCCESS),
                    null,
                    null
            );
        }
        else {
            return errorHandler.handleError(ex);
        }
    }
}
package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.submit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.DefaultSubmitPaymentExecutionContextErrorHandler;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionResult;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Clock;
import java.time.Instant;

public class YoltBankSubmitErrorHandler extends DefaultSubmitPaymentExecutionContextErrorHandler {

    private final Clock clock;

    public YoltBankSubmitErrorHandler(RawBankPaymentStatusMapper rawBankPaymentStatusMapper, ObjectMapper objectMapper, Clock clock) {
        super(rawBankPaymentStatusMapper, objectMapper, clock);
        this.clock = clock;
    }

    @Override
    @SneakyThrows
    public <Request, Response, Intermediate> PaymentExecutionResult<Response, Intermediate> handleError(HttpEntity<Request> httpEntity, Instant requestTimestamp, Exception e) {
        if (e instanceof HttpStatusCodeException) {
            var httpStatusCode = ((HttpStatusCodeException) e).getStatusCode();
            switch (httpStatusCode) {
                /**
                 * See PaymentExecutionErrorException in backend/yoltbank
                 */
                case I_AM_A_TEAPOT:
                    return response(EnhancedPaymentStatus.EXECUTION_FAILED);
                /**
                 * See PaymentNoConsentFromUserException in backend/yoltbank
                 */
                case UNAVAILABLE_FOR_LEGAL_REASONS:
                    return response(EnhancedPaymentStatus.NO_CONSENT_FROM_USER);
                /**
                 * See PaymentRejectedException in backend/yoltbank
                 */
                case PAYMENT_REQUIRED:
                    return response(EnhancedPaymentStatus.REJECTED);
            }
        }

        return super.handleError(httpEntity, requestTimestamp, e);
    }

    <R, I> PaymentExecutionResult<R, I> response(EnhancedPaymentStatus status) {
        return new PaymentExecutionResult<R, I>(
                Instant.now(clock),
                Instant.now(clock),
                "",
                "",
                "",
                "",
                new PaymentStatuses(
                        RawBankPaymentStatus.unknown(),
                        status
                ),
                null,
                null
        );
    }
}

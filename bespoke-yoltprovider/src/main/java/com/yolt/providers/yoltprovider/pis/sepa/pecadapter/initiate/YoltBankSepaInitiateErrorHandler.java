package com.yolt.providers.yoltprovider.pis.sepa.pecadapter.initiate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.DefaultInitiatePaymentExecutionContextErrorHandler;
import com.yolt.providers.common.pis.paymentexecutioncontext.errorhandler.RawBankPaymentStatusMapper;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.EnhancedPaymentStatus;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentExecutionResult;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.PaymentStatuses;
import com.yolt.providers.common.pis.paymentexecutioncontext.model.RawBankPaymentStatus;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.Clock;
import java.time.Instant;

public class YoltBankSepaInitiateErrorHandler extends DefaultInitiatePaymentExecutionContextErrorHandler {

    private final Clock clock;

    public YoltBankSepaInitiateErrorHandler(RawBankPaymentStatusMapper rawBankPaymentStatusMapper, ObjectMapper objectMapper, Clock clock) {
        super(rawBankPaymentStatusMapper, objectMapper, clock);
        this.clock = clock;
    }

    @Override
    public <Request, Response, Intermediate> PaymentExecutionResult<Response, Intermediate> handleError(HttpEntity<Request> httpEntity, Instant requestTimestamp, Exception e) {
        if (e instanceof HttpStatusCodeException) {
            var httpStatusCode = ((HttpStatusCodeException) e).getStatusCode();
            switch (httpStatusCode) {
                /**
                 * See PaymentInitiationErrorException in backend/yoltbank
                 */
                case URI_TOO_LONG:
                    return new PaymentExecutionResult<Response, Intermediate>(
                            requestTimestamp,
                            Instant.now(clock),
                            "",
                            "",
                            "",
                            "",
                            new PaymentStatuses(
                                    RawBankPaymentStatus.unknown(),
                                    EnhancedPaymentStatus.INITIATION_ERROR
                            ),
                            null,
                            null
                    );
            }
        }

        return super.handleError(httpEntity, requestTimestamp, e);
    }
}

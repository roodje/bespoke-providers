package com.yolt.providers.stet.generic.service.payment.rest.error;

import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
class DefaultPaymentHttpErrorHandlerTest {

    private DefaultPaymentHttpErrorHandler errorHandler;

    @BeforeEach
    void initialize() {
        errorHandler = new DefaultPaymentHttpErrorHandler();
    }

    @Test
    void shouldThrowCreationFailedExceptionForAnyHttpStatusCodeExceptionAndInitiatePaymentPrometheusPath() {
        // given
        HttpClientErrorException httpStatus = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        ExecutionInfo executionInfo = createExecutionInfo(INITIATE_PAYMENT);

        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(httpStatus, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(CreationFailedException.class);
    }

    @Test
    void shouldThrowConfirmationFailedExceptionForAnyHttpStatusCodeExceptionAndSubmitPaymentPrometheusPath() {
        // given
        HttpClientErrorException httpStatus = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        ExecutionInfo executionInfo = createExecutionInfo(SUBMIT_PAYMENT);

        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(httpStatus, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(ConfirmationFailedException.class);
    }

    @Test
    void shouldThrowHttpStatusCodeExceptionForAnyHttpStatusCodeExceptionAndGetPaymentStatusPrometheusPath() {
        // given
        HttpClientErrorException httpStatus = new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        ExecutionInfo executionInfo = createExecutionInfo(GET_PAYMENT_STATUS);

        // when
        ThrowingCallable throwingCallable = () -> errorHandler.handle(httpStatus, executionInfo);

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(HttpStatusCodeException.class);
    }

    private ExecutionInfo createExecutionInfo(String prometheusPath) {
        return new ExecutionInfo("/example", HttpMethod.POST, new HttpHeaders(), prometheusPath);
    }
}

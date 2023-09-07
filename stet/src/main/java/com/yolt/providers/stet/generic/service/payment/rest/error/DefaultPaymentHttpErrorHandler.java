package com.yolt.providers.stet.generic.service.payment.rest.error;

import com.yolt.providers.common.exception.ConfirmationFailedException;
import com.yolt.providers.common.exception.CreationFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.DefaultHttpErrorHandler;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.*;

/**
 * @deprecated It should be removed after migration of all STET generic providers to PEC (Payment Execution Context).
 * TODO: Required tickets to be done before deleting this class: C4PO-8635, C4PO-8457, C4PO-8451
 */
@Deprecated
public class DefaultPaymentHttpErrorHandler extends DefaultHttpErrorHandler {

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public <T> T executeAndHandle(ExecutionSupplier<T> execution, ExecutionInfo executionInfo) {
        return super.executeAndHandle(execution, executionInfo);
    }

    @Override
    @SneakyThrows({CreationFailedException.class, ConfirmationFailedException.class})
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) {
        HttpStatus status = e.getStatusCode();
        switch (executionInfo.getPrometheusPathOverride()) {
            case INITIATE_PAYMENT:
                throw new CreationFailedException("Failed to initiate payment: HTTP " + status.value());
            case SUBMIT_PAYMENT:
                throw new ConfirmationFailedException("Failed to confirm payment: HTTP " + status.value());
            case CLIENT_CREDENTIALS_GRANT:
                throw new IllegalStateException("Failed to get client credentials token: HTTP " + status.value());
            default:
                throw e;
        }
    }
}

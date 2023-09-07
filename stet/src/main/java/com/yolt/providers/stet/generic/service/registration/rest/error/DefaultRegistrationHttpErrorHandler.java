package com.yolt.providers.stet.generic.service.registration.rest.error;

import com.yolt.providers.common.exception.ProviderRequestFailedException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.DefaultHttpErrorHandler;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import lombok.SneakyThrows;
import org.springframework.web.client.HttpStatusCodeException;

import static com.yolt.providers.common.rest.http.ProviderClientEndpoints.REGISTER;

public class DefaultRegistrationHttpErrorHandler extends DefaultHttpErrorHandler {

    @Override
    @SneakyThrows(TokenInvalidException.class)
    public <T> T executeAndHandle(ExecutionSupplier<T> execution, ExecutionInfo executionInfo) {
        return super.executeAndHandle(execution, executionInfo);
    }

    @Override
    public void handle(HttpStatusCodeException e, ExecutionInfo executionInfo) {
        switch (executionInfo.getPrometheusPathOverride()) {
            case REGISTER:
                throw new ProviderRequestFailedException("Something went wrong while trying to register provider", e);
            case "update_registration":
                throw new ProviderRequestFailedException("Something went wrong while trying to update provider registration", e);
            case "delete_registration":
                throw new ProviderRequestFailedException("Something went wrong while trying to remove provider registration", e);
            default:
                throw e;
        }
    }
}

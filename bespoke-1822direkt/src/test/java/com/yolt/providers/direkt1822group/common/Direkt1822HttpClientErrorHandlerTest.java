package com.yolt.providers.direkt1822group.common;

import com.yolt.providers.common.exception.BackPressureRequestException;
import com.yolt.providers.direkt1822group.common.rest.Direkt1822HttpClientErrorHandler;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class Direkt1822HttpClientErrorHandlerTest {

    Direkt1822HttpClientErrorHandler handler = new Direkt1822HttpClientErrorHandler();

    @Test
    public void shouldThrowBackPressureRequestExceptionWhenHttpStatusIs429() {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> handler.handle(new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS));

        // then
        assertThatThrownBy(throwingCallable).isInstanceOf(BackPressureRequestException.class);
    }
}

package com.yolt.providers.stet.creditagricolegroup.common.service.fetchdata.error;

import com.yolt.providers.common.exception.ProviderHttpStatusException;
import com.yolt.providers.common.exception.TokenInvalidException;
import com.yolt.providers.stet.generic.domain.ExecutionInfo;
import com.yolt.providers.stet.generic.http.error.ExecutionSupplier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

class CreditAgricoleGroupFetchDataErrorHandlerTest {
    private final CreditAgricoleGroupFetchDataHttpErrorHandler creditAgricoleGroupFetchDataErrorHandlerTest = new CreditAgricoleGroupFetchDataHttpErrorHandler();
    private final ExecutionInfo executionInfo = Mockito.mock(ExecutionInfo.class);

    @Test
    public void shouldCallTenTimes() throws TokenInvalidException {
        //given
        String exceptionBody = "{\"status\":\"500\",\"error\":\"303001\"}";
        HttpStatus exceptionStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ExecutionSupplier mockedSupplier = Mockito.mock(ExecutionSupplier.class);
        HttpStatusCodeException mockedException = Mockito.mock(HttpStatusCodeException.class);
        Mockito.when(mockedException.getStatusCode()).thenReturn(exceptionStatus);
        Mockito.when(mockedException.getResponseBodyAsString()).thenReturn(exceptionBody);
        Mockito.when(mockedSupplier.get()).thenThrow(mockedException);
        //when
        Assertions.assertThatThrownBy(() -> creditAgricoleGroupFetchDataErrorHandlerTest.executeAndHandle(mockedSupplier, executionInfo))
                .isInstanceOf(ProviderHttpStatusException.class);
        //then
        Mockito.verify(mockedSupplier, Mockito.times(10)).get();
    }

    @Test
    public void shouldCallOnlyOne() throws TokenInvalidException {
        //given
        String exceptionBody = "body";
        HttpStatus exceptionStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        ExecutionSupplier mockedSupplier = Mockito.mock(ExecutionSupplier.class);
        HttpStatusCodeException mockedException = Mockito.mock(HttpStatusCodeException.class);
        Mockito.when(mockedException.getStatusCode()).thenReturn(exceptionStatus);
        Mockito.when(mockedException.getResponseBodyAsString()).thenReturn(exceptionBody);
        Mockito.when(mockedSupplier.get()).thenThrow(mockedException);
        //when
        Assertions.assertThatThrownBy(() -> creditAgricoleGroupFetchDataErrorHandlerTest.executeAndHandle(mockedSupplier, executionInfo))
                .isInstanceOf(ProviderHttpStatusException.class);
        //then
        Mockito.verify(mockedSupplier, Mockito.times(1)).get();
    }

}

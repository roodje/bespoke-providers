package com.yolt.providers.openbanking.ais.generic2.pec.status.single;

import com.yolt.providers.openbanking.ais.generic2.auth.DefaultAuthMeans;
import com.yolt.providers.openbanking.ais.generic2.pec.common.PaymentHttpHeadersFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class GenericPaymentStatusHttpHeadersProviderTest {

    @InjectMocks
    private GenericPaymentStatusHttpHeadersProvider subject;

    @Mock
    private PaymentHttpHeadersFactory httpHeadersFactory;

    @Test
    void shouldReturnProperHttpHeadersWhenCorrectDataAreProvided() {
        // given
        DefaultAuthMeans authMeans = DefaultAuthMeans.builder().build();
        GenericPaymentStatusPreExecutionResult preExecutionResult = createGenericStatusPaymentPreExecutionResult(authMeans);
        HttpHeaders expectedHttpHeaders = new HttpHeaders();

        given(httpHeadersFactory.createCommonPaymentHttpHeaders(anyString(), any(DefaultAuthMeans.class)))
                .willReturn(expectedHttpHeaders);

        // when
        HttpHeaders result = subject.provideHttpHeaders(preExecutionResult, null);

        // then
        then(httpHeadersFactory)
                .should()
                .createCommonPaymentHttpHeaders("accessToken", authMeans);

        assertThat(result).isEqualTo(expectedHttpHeaders);
    }

    private GenericPaymentStatusPreExecutionResult createGenericStatusPaymentPreExecutionResult(DefaultAuthMeans authMeans) {
        return new GenericPaymentStatusPreExecutionResult("accessToken", authMeans, null, null, null);
    }
}
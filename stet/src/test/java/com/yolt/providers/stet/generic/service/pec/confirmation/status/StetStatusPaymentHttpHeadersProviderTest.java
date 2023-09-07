package com.yolt.providers.stet.generic.service.pec.confirmation.status;

import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
import com.yolt.providers.stet.generic.service.pec.confirmation.StetConfirmationPreExecutionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class StetStatusPaymentHttpHeadersProviderTest {

    @Mock
    private StetPaymentHttpHeadersFactory httpHeadersFactory;

    private StetStatusPaymentHttpHeadersProvider stetStatusPaymentHttpHeadersProvider;

    @BeforeEach
    void initialize() {
        stetStatusPaymentHttpHeadersProvider = new StetStatusPaymentHttpHeadersProvider(httpHeadersFactory);
    }

    @Test
    void shouldProvideHttpHeadersForPaymentStatus() {
        // given
        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .build();

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        given(httpHeadersFactory.createPaymentStatusHttpHeaders(any(StetConfirmationPreExecutionResult.class)))
                .willReturn(expectedHttpHeaders);

        // when
        HttpHeaders httpHeaders = stetStatusPaymentHttpHeadersProvider.provideHttpHeaders(preExecutionResult, null);

        // then
        assertThat(httpHeaders).containsExactlyEntriesOf(expectedHttpHeaders);
    }
}

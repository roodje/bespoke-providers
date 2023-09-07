package com.yolt.providers.stet.generic.service.pec.confirmation.submit;

import com.yolt.providers.stet.generic.dto.payment.StetPaymentConfirmationRequestDTO;
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
public class StetSubmitPaymentHttpHeadersProviderTest {

    @Mock
    private StetPaymentHttpHeadersFactory httpHeadersFactory;

    private StetSubmitPaymentHttpHeadersProvider submitPaymentHttpHeadersProvider;

    @BeforeEach
    void initialize() {
        submitPaymentHttpHeadersProvider = new StetSubmitPaymentHttpHeadersProvider(httpHeadersFactory);
    }

    @Test
    void shouldProvideHttpHeadersForPaymentStatus() {
        // given
        StetConfirmationPreExecutionResult preExecutionResult = StetConfirmationPreExecutionResult.builder()
                .build();

        StetPaymentConfirmationRequestDTO requestDTO = StetPaymentConfirmationRequestDTO.builder()
                .build();

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        given(httpHeadersFactory.createPaymentSubmitHttpHeaders(any(StetConfirmationPreExecutionResult.class), any(StetPaymentConfirmationRequestDTO.class)))
                .willReturn(expectedHttpHeaders);

        // when
        HttpHeaders httpHeaders = submitPaymentHttpHeadersProvider.provideHttpHeaders(preExecutionResult, requestDTO);

        // then
        assertThat(httpHeaders).containsExactlyEntriesOf(expectedHttpHeaders);
    }
}

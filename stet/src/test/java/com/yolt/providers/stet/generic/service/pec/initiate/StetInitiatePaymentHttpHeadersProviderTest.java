package com.yolt.providers.stet.generic.service.pec.initiate;

import com.yolt.providers.stet.generic.dto.payment.StetPaymentInitiationRequestDTO;
import com.yolt.providers.stet.generic.service.pec.common.StetPaymentHttpHeadersFactory;
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
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class StetInitiatePaymentHttpHeadersProviderTest {

    @Mock
    private StetPaymentHttpHeadersFactory httpHeadersFactory;

    private StetInitiatePaymentHttpHeadersProvider initiatePaymentHttpHeadersProvider;

    @BeforeEach
    void setUp() {
        initiatePaymentHttpHeadersProvider = new StetInitiatePaymentHttpHeadersProvider(httpHeadersFactory);
    }

    @Test
    void shouldProvideHttpHeadersForPaymentInitiation() {
        // given
        StetInitiatePreExecutionResult preExecutionResult = StetInitiatePreExecutionResult.builder()
                .build();

        StetPaymentInitiationRequestDTO requestDTO = StetPaymentInitiationRequestDTO.builder()
                .build();

        HttpHeaders expectedHttpHeaders = new HttpHeaders();
        expectedHttpHeaders.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        given(httpHeadersFactory.createPaymentInitiationHttpHeaders(any(StetInitiatePreExecutionResult.class), any(StetPaymentInitiationRequestDTO.class)))
                .willReturn(expectedHttpHeaders);

        // when
        HttpHeaders httpHeaders = initiatePaymentHttpHeadersProvider.provideHttpHeaders(preExecutionResult, requestDTO);

        // then
        assertThat(httpHeaders).containsExactlyEntriesOf(expectedHttpHeaders);

        then(httpHeadersFactory)
                .should()
                .createPaymentInitiationHttpHeaders(preExecutionResult, requestDTO);
    }
}

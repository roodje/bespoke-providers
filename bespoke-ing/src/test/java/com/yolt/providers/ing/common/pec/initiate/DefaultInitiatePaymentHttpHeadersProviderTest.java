package com.yolt.providers.ing.common.pec.initiate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import com.yolt.providers.ing.common.pec.DefaultCommonHttpHeadersProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static com.yolt.providers.ing.common.pec.IngPecConstants.INITIATE_PAYMENT_ENDPOINT;
import static com.yolt.providers.ing.common.pec.IngPecConstants.INITIATE_PAYMENT_HTTP_METHOD;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultInitiatePaymentHttpHeadersProviderTest {

    @InjectMocks
    private DefaultInitiatePaymentHttpHeadersProvider sut;

    @Mock
    private DefaultCommonHttpHeadersProvider commonHttpHeadersProvider;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void shouldReturnHttpHeaderForCorrectData() throws JsonProcessingException {
        // given
        DefaultInitiatePaymentPreExecutionResult preExecutionResult = createPreExecutionResult();
        HttpHeaders expectedHeaders = createExpectedHeaders();

        given(objectMapper.writeValueAsString(any(SepaCreditTransfer.class)))
                .willReturn("");
        given(commonHttpHeadersProvider.provideHttpHeaders(any(DefaultInitiatePaymentPreExecutionResult.class), any(), eq(INITIATE_PAYMENT_HTTP_METHOD), eq(INITIATE_PAYMENT_ENDPOINT)))
                .willReturn(new HttpHeaders());

        // when
        HttpHeaders result = sut.provideHttpHeaders(preExecutionResult, new SepaCreditTransfer());

        // then
        Assertions.assertThat(result).isEqualTo(expectedHeaders);
    }

    private HttpHeaders createExpectedHeaders() {
        var headers = new HttpHeaders();
        headers.add("TPP-Redirect-URI", "https://localhost.com?state=fakeState");
        return headers;
    }

    private DefaultInitiatePaymentPreExecutionResult createPreExecutionResult() {
        return new DefaultInitiatePaymentPreExecutionResult(
                null,
                null,
                null,
                null,
                null,
                "https://localhost.com",
                "fakeState",
                null
        );
    }
}
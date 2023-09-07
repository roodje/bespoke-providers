package com.yolt.providers.ing.common.pec.submit;

import com.yolt.providers.ing.common.pec.DefaultCommonHttpHeadersProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static com.yolt.providers.ing.common.pec.IngPecConstants.SUBMIT_PAYMENT_ENDPOINT;
import static com.yolt.providers.ing.common.pec.IngPecConstants.SUBMIT_PAYMENT_HTTP_METHOD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class DefaultSubmitPaymentHttpHeadersProviderTest {

    @InjectMocks
    private DefaultSubmitPaymentHttpHeadersProvider sut;

    @Mock
    private DefaultCommonHttpHeadersProvider commonHttpHeadersProvider;

    @Test
    public void shouldReturnHttpHeaderForCorrectData() {
        // given
        DefaultSubmitPaymentPreExecutionResult preExecutionResult = createPreExecutionResult();
        HttpHeaders expectedHeaders = new HttpHeaders();
        String expectedFormattedEndpointPath = String.format(SUBMIT_PAYMENT_ENDPOINT, preExecutionResult.getPaymentId());

        given(commonHttpHeadersProvider.provideHttpHeaders(any(DefaultSubmitPaymentPreExecutionResult.class), eq(new byte[0]), eq(SUBMIT_PAYMENT_HTTP_METHOD), eq(expectedFormattedEndpointPath)))
                .willReturn(new HttpHeaders());

        // when
        HttpHeaders result = sut.provideHttpHeaders(preExecutionResult, null);

        // then
        Assertions.assertThat(result).isEqualTo(expectedHeaders);
    }

    private DefaultSubmitPaymentPreExecutionResult createPreExecutionResult() {
        return new DefaultSubmitPaymentPreExecutionResult(
                "fakePaymentId",
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
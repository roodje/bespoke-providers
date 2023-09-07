package com.yolt.providers.yoltprovider.pis.ukdomestic.pecadapter.submit;

import com.yolt.providers.yoltprovider.pis.ukdomestic.ConfirmPaymentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.yolt.providers.common.pis.common.PaymentType.SINGLE;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class YoltBankUkSubmitPaymentHttpRequestBodyProviderTest {

    @InjectMocks
    private YoltBankUkSubmitPaymentHttpRequestBodyProvider subject;

    @Test
    void shouldReturnConfirmPaymentRequestForProvideHttpRequestBodyWhenCorrectData() {
        // given
        UUID paymentId = UUID.randomUUID();
        YoltBankUkSubmitPreExecutionResult yoltBankUkSubmitPreExecutionResult = new YoltBankUkSubmitPreExecutionResult(
                paymentId,
                null,
                SINGLE
        );

        // when
        ConfirmPaymentRequest result = subject.provideHttpRequestBody(yoltBankUkSubmitPreExecutionResult);

        // then
        assertThat(result.getPaymentId()).isEqualTo(paymentId);
    }
}
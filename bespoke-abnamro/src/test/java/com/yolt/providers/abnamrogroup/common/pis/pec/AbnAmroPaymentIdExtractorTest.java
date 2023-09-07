package com.yolt.providers.abnamrogroup.common.pis.pec;

import com.yolt.providers.abnamro.pis.TransactionStatusResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AbnAmroPaymentIdExtractorTest {

    @InjectMocks
    private AbnAmroPaymentIdExtractor<String> subject;

    @Test
    void shouldReturnTransactionIdForExtractPaymentIdWhenCorrectData() {
        // given
        TransactionStatusResponse transactionStatusResponse = new TransactionStatusResponse();
        transactionStatusResponse.setTransactionId("transactionId");

        // when
        String result = subject.extractPaymentId(transactionStatusResponse, null);

        // then
        assertThat(result).isEqualTo("transactionId");
    }
}
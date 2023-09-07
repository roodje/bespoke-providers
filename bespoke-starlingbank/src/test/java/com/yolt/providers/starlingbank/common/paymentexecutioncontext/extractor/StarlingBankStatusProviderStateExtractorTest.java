package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.starlingbank.common.model.PaymentStatusResponse;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StarlingBankStatusProviderStateExtractorTest {

    @InjectMocks
    private StarlingBankStatusPaymentIdExtractor providerStateExtractor;

    @Test
    void shouldExtractPaymentId() {
        //given
        InitiateUkDomesticPaymentRequestDTO paymentRequestDTO = new InitiateUkDomesticPaymentRequestDTO("ENDTOEND",
                "EUR",
                new BigDecimal("12.2"),
                new UkAccountDTO("123", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, " Holder", null),
                null,
                "Remittance Information",
                null);
        PaymentStatusResponse responseBody = new PaymentStatusResponse();
        StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult =
                StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult.builder()
                        .externalPaymentId("EXTERNAL_PAYMENT_ID")
                        .paymentRequest(paymentRequestDTO)
                        .build();

        //when
        String result = providerStateExtractor.extractPaymentId(responseBody, preExecutionResult);

        //then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("EXTERNAL_PAYMENT_ID");
    }
}

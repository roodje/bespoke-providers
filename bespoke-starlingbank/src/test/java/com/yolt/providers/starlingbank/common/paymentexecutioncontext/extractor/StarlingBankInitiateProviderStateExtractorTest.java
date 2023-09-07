package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.starlingbank.common.model.UkDomesticPaymentProviderState;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankInitiatePaymentExecutionContextPreExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StarlingBankInitiateProviderStateExtractorTest {
    private static final String PAYMENT_ORDER_UUID = UUID.randomUUID().toString();

    @InjectMocks
    private StarlingBankInitiatePaymentProviderStateExtractor providerStateExtractor;

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
        String responseBody = "response";
        StarlingBankInitiatePaymentExecutionContextPreExecutionResult preExecutionResult =
                StarlingBankInitiatePaymentExecutionContextPreExecutionResult.builder()
                        .baseRedirectUrl("")
                        .providerStatePayload(UkDomesticPaymentProviderState.builder()
                                .externalPaymentId(PAYMENT_ORDER_UUID)
                                .paymentRequest(paymentRequestDTO)
                                .build())
                        .build();
        //when
        UkProviderState result = providerStateExtractor.extractUkProviderState(responseBody, preExecutionResult);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getConsentId()).isNull();
        assertThat(result.getOpenBankingPayment()).isEqualTo(paymentRequestDTO);
    }
}

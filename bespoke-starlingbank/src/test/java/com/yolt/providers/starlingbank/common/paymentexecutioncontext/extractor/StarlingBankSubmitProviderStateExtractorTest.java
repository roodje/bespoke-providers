package com.yolt.providers.starlingbank.common.paymentexecutioncontext.extractor;

import com.yolt.providers.common.pis.common.PaymentType;
import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.common.pis.ukdomestic.UkProviderState;
import com.yolt.providers.starlingbank.common.model.PaymentSubmissionResponse;
import com.yolt.providers.starlingbank.common.model.UkDomesticPaymentProviderState;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class StarlingBankSubmitProviderStateExtractorTest {

    @InjectMocks
    private StarlingBankSubmitPaymentProviderStateExtractor providerStateExtractor;

    @Test
    void shouldExtractProviderState() {
        //given
        InitiateUkDomesticPaymentRequestDTO paymentRequestDTO = new InitiateUkDomesticPaymentRequestDTO("ENDTOEND",
                "EUR",
                new BigDecimal("12.2"),
                new UkAccountDTO("123", AccountIdentifierScheme.SORTCODEACCOUNTNUMBER, " Holder", null),
                null,
                "Remittance Information",
                null);
        UUID receivedPaymentOrderId = UUID.randomUUID();
        PaymentSubmissionResponse responseBody = new PaymentSubmissionResponse(receivedPaymentOrderId, null);
        Date tokenExpirationTime = Date.from(Instant.now().plus(10, ChronoUnit.SECONDS));
        StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult preExecutionResult =
                StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult.builder()
                        .externalPaymentId("EXTERNAL_PAYMENT_ID")
                        .expiresIn(tokenExpirationTime)
                        .token("THE-ACCESS-TOKEN")
                        .refreshToken("THE-REFRESH-TOKEN")
                        .paymentRequest(paymentRequestDTO)
                        .build();

        UkProviderState expectedState = new UkProviderState(null,
                PaymentType.SINGLE,
                UkDomesticPaymentProviderState.builder()
                        .externalPaymentId(receivedPaymentOrderId.toString())
                        .accessTokenExpiresIn(tokenExpirationTime)
                        .refreshToken("THE-REFRESH-TOKEN")
                        .accessToken("THE-ACCESS-TOKEN")
                        .build());

        //when
        UkProviderState result = providerStateExtractor.extractUkProviderState(responseBody, preExecutionResult);

        //then
        assertThat(result).isEqualTo(expectedState);
    }
}

package com.yolt.providers.knabgroup.common.payment.initiate;

import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.knabgroup.common.payment.dto.Internal.InitiatePaymentPreExecutionResult;
import com.yolt.providers.knabgroup.common.payment.dto.external.InitiatePaymentRequestBody;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultInitiatePaymentHttpRequestBodyProviderTest {

    private DefaultInitiatePaymentHttpRequestBodyProvider subject;

    @Test
    void shouldReturnSepaPaymentForProvideHttpRequestBodyWhenCorrectData() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = createRequestDTO();
        InitiatePaymentPreExecutionResult preExecutionResult = createPreExecutionResult(requestDTO);
        subject = new DefaultInitiatePaymentHttpRequestBodyProvider();
        InitiatePaymentRequestBody expectedResult = new InitiatePaymentRequestBody(
                new InitiatePaymentRequestBody.BankAccount("creditorIban"),
                new InitiatePaymentRequestBody.BankAccount("debtorIban"),
                new InitiatePaymentRequestBody.InstructedAmount("100.00", "EUR"),
                "Creditor",
                "remittance info"
        );

        // when
        InitiatePaymentRequestBody result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result).usingRecursiveComparison()
                .isEqualTo(expectedResult);
    }

    private SepaInitiatePaymentRequestDTO createRequestDTO() {
        return SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(SepaAccountDTO.builder()
                        .iban("debtorIban")
                        .currency(CurrencyCode.EUR)
                        .build())
                .creditorAccount(SepaAccountDTO.builder()
                        .iban("creditorIban")
                        .currency(CurrencyCode.EUR)
                        .build())
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("100.00"))
                        .build())
                .creditorName("Creditor")
                .remittanceInformationUnstructured("remittance info")
                .build();
    }

    private InitiatePaymentPreExecutionResult createPreExecutionResult(SepaInitiatePaymentRequestDTO requestDTO) {
        return new InitiatePaymentPreExecutionResult(
                requestDTO,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

}
package com.yolt.providers.abnamrogroup.common.pis.pec.initiate;

import com.yolt.providers.abnamro.pis.SepaPayment;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AbnAmroInitiatePaymentHttpRequestBodyProviderTest {

    @InjectMocks
    private AbnAmroInitiatePaymentHttpRequestBodyProvider subject;

    @Test
    void shouldReturnSepaPaymentForProvideHttpRequestBodyWhenCorrectData() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = createRequestDTO();
        AbnAmroInitiatePaymentPreExecutionResult preExecutionResult = createPreExecutionResult(requestDTO);

        // when
        SepaPayment result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result).extracting(SepaPayment::getInitiatingpartyAccountNumber,
                SepaPayment::getCounterpartyAccountNumber,
                SepaPayment::getAmount,
                SepaPayment::getCounterpartyName,
                SepaPayment::getRemittanceInfo)
                .contains("debtorIban", "creditorIban", Float.valueOf("100.00"), "Creditor", "remittance info");
    }

    @Test
    void shouldReturnSepaPaymentForProvideHttpRequestWithoutDebtor() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = createRequestDTOWithoutDebtor();
        AbnAmroInitiatePaymentPreExecutionResult preExecutionResult = createPreExecutionResult(requestDTO);

        // when
        SepaPayment result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result).extracting(SepaPayment::getInitiatingpartyAccountNumber,
                SepaPayment::getCounterpartyAccountNumber,
                SepaPayment::getAmount,
                SepaPayment::getCounterpartyName,
                SepaPayment::getRemittanceInfo)
                .contains("creditorIban", Float.valueOf("100.00"), "Creditor", "remittance info");
    }

        private SepaInitiatePaymentRequestDTO createRequestDTO() {
        return SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(SepaAccountDTO.builder()
                        .iban("debtorIban")
                        .build())
                .creditorAccount(SepaAccountDTO.builder()
                        .iban("creditorIban")
                        .build())
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("100.00"))
                        .build())
                .creditorName("Creditor")
                .remittanceInformationUnstructured("remittance info")
                .build();
    }

    private SepaInitiatePaymentRequestDTO createRequestDTOWithoutDebtor() {
        return SepaInitiatePaymentRequestDTO.builder()
                .creditorAccount(SepaAccountDTO.builder()
                        .iban("creditorIban")
                        .build())
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("100.00"))
                        .build())
                .creditorName("Creditor")
                .remittanceInformationUnstructured("remittance info")
                .build();
    }

    private AbnAmroInitiatePaymentPreExecutionResult createPreExecutionResult(SepaInitiatePaymentRequestDTO requestDTO) {
        return new AbnAmroInitiatePaymentPreExecutionResult(
                "",
                null,
                null,
                requestDTO,
                "",
                ""
        );
    }
}
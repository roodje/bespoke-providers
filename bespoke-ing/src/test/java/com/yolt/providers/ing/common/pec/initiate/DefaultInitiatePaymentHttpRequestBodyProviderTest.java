package com.yolt.providers.ing.common.pec.initiate;

import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.ing.common.dto.CreditorAccount;
import com.yolt.providers.ing.common.dto.DebtorAccount;
import com.yolt.providers.ing.common.dto.InstructedAmount;
import com.yolt.providers.ing.common.dto.SepaCreditTransfer;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultInitiatePaymentHttpRequestBodyProviderTest {

    private DefaultInitiatePaymentHttpRequestBodyProvider sut;

    @Test
    void shouldReturnSepaPaymentForProvideHttpRequestBodyWhenCorrectData() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = createRequestDTO();
        DefaultInitiatePaymentPreExecutionResult preExecutionResult = createPreExecutionResult(requestDTO);
        sut = new DefaultInitiatePaymentHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyMapper());

        // when
        SepaCreditTransfer result = sut.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result.getCreditorAccount())
                .extracting(CreditorAccount::getCurrency, CreditorAccount::getIban)
                .contains(CurrencyCode.EUR.name(), "creditorIban");
        assertThat(result.getDebtorAccount())
                .extracting(DebtorAccount::getCurrency, DebtorAccount::getIban)
                .contains(CurrencyCode.EUR.name(), "debtorIban");
        assertThat(result.getInstructedAmount())
                .extracting(InstructedAmount::getCurrency, InstructedAmount::getAmount)
                .contains(CurrencyCode.EUR.name(), "100.00");

        assertThat(result).extracting(SepaCreditTransfer::getCreditorName,
                        SepaCreditTransfer::getEndToEndIdentification,
                        SepaCreditTransfer::getRemittanceInformationUnstructured)
                .contains("Creditor", "endToEnd info", "remittance info");
    }

    @Test
    void shouldReturnSepaScheduledPaymentForProvideHttpRequestBodyWhenCorrectData() {
        // given
        SepaInitiatePaymentRequestDTO requestDTO = createScheduledPaymentRequestDTO();
        DefaultInitiatePaymentPreExecutionResult preExecutionResult = createPreExecutionResult(requestDTO);
        sut = new DefaultInitiatePaymentHttpRequestBodyProvider(new DefaultInitiatePaymentHttpRequestBodyMapper());

        // when
        SepaCreditTransfer result = sut.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result.getCreditorAccount())
                .extracting(CreditorAccount::getCurrency, CreditorAccount::getIban)
                .contains(CurrencyCode.EUR.name(), "creditorIban");
        assertThat(result.getDebtorAccount())
                .extracting(DebtorAccount::getCurrency, DebtorAccount::getIban)
                .contains(CurrencyCode.EUR.name(), "debtorIban");
        assertThat(result.getInstructedAmount())
                .extracting(InstructedAmount::getCurrency, InstructedAmount::getAmount)
                .contains(CurrencyCode.EUR.name(), "100.00");

        assertThat(result).extracting(SepaCreditTransfer::getCreditorName,
                        SepaCreditTransfer::getEndToEndIdentification,
                        SepaCreditTransfer::getRemittanceInformationUnstructured,
                        SepaCreditTransfer::getRequestedExecutionDate)
                .contains("Creditor", "endToEnd info", "remittance info", "2021-12-20");
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
                .endToEndIdentification("endToEnd info")
                .build();
    }

    private SepaInitiatePaymentRequestDTO createScheduledPaymentRequestDTO() {
        SepaInitiatePaymentRequestDTO sepaInitiatePaymentRequestDTO = createRequestDTO();
        sepaInitiatePaymentRequestDTO.setExecutionDate(LocalDate.of(2021, 12, 20));
        return sepaInitiatePaymentRequestDTO;
    }

    private DefaultInitiatePaymentPreExecutionResult createPreExecutionResult(SepaInitiatePaymentRequestDTO requestDTO) {
        return new DefaultInitiatePaymentPreExecutionResult(
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
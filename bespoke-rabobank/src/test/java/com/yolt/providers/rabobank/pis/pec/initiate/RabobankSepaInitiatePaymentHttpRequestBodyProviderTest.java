package com.yolt.providers.rabobank.pis.pec.initiate;

import com.yolt.providers.common.pis.sepa.DynamicFields;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.rabobank.dto.external.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class RabobankSepaInitiatePaymentHttpRequestBodyProviderTest {

    private RabobankSepaInitiatePaymentHttpRequestBodyProvider subject;

    @BeforeEach
    void setUp() {
        subject = new RabobankSepaInitiatePaymentHttpRequestBodyProvider();
    }

    @Test
    void shouldReturnMappedSepaPaymentWithCorrectDate() {
        //given
        RabobankSepaInitiatePreExecutionResult preExecutionResult = preparePreExecutionResult();

        //when
        SepaCreditTransfer mappedRequest = subject.provideHttpRequestBody(preExecutionResult);

        //then

        assertThat(mappedRequest.getCreditorAccount()).extracting(CreditorAccount::getCurrency,
                CreditorAccount::getIban)
                .contains("GBP", "creditorIban");
        assertThat(mappedRequest.getDebtorAccount()).extracting(DebtorAccount::getCurrency,
                DebtorAccount::getIban)
                .contains("EUR", "debtorIban");
        assertThat(mappedRequest.getInstructedAmount()).extracting(InstructedAmount::getCurrency,
                InstructedAmount::getContent)
                .contains("EUR", "123.33");
        assertThat(mappedRequest.getCreditorAddress()).extracting(CreditorAddress::getCountry)
                .isEqualTo("NL");
        assertThat(mappedRequest).extracting(SepaCreditTransfer::getCreditorName,
                SepaCreditTransfer::getRemittanceInformationUnstructured,
                SepaCreditTransfer::getEndToEndIdentification)
                .contains("Janusz Kowalski", "Some remittance information", "123-555-444");

    }

    private RabobankSepaInitiatePreExecutionResult preparePreExecutionResult() {
        return new RabobankSepaInitiatePreExecutionResult(null,
                prepareRequestDto(),
                null,
                null,
                null,
                null,
                null);
    }

    private SepaInitiatePaymentRequestDTO prepareRequestDto() {
        DynamicFields dynamicFields = new DynamicFields();
        dynamicFields.setCreditorPostalCountry("NL");
        return SepaInitiatePaymentRequestDTO.builder()
                .debtorAccount(SepaAccountDTO.builder()
                        .iban("debtorIban")
                        .currency(CurrencyCode.EUR)
                        .build())
                .creditorAccount(SepaAccountDTO.builder()
                        .iban("creditorIban")
                        .currency(CurrencyCode.GBP)
                        .build())
                .creditorName("Janusz Kowalski")
                .instructedAmount(SepaAmountDTO.builder()
                        .amount(new BigDecimal("123.33"))
                        .build())
                .endToEndIdentification("123-555-444")
                .remittanceInformationUnstructured("Some remittance information")
                .dynamicFields(dynamicFields)
                .build();
    }
}

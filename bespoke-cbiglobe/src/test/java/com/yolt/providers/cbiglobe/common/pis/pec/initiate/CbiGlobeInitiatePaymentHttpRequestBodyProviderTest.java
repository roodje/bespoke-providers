package com.yolt.providers.cbiglobe.common.pis.pec.initiate;

import com.yolt.providers.cbiglobe.posteitaliane.pis.pec.initiate.PosteItalianeInstructedAmountToCurrencyMapper;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CbiGlobeInitiatePaymentHttpRequestBodyProviderTest {

    private CbiGlobeInitiatePaymentHttpRequestBodyProvider subject = new CbiGlobeInitiatePaymentHttpRequestBodyProvider(
            new CbiGlobeDefaultAccountToCurrencyMapper(),
            new CbiGlobeDefaultInstructedAmountToCurrencyMapper()
    );

    @Test
    void shouldReturnInitiatePaymentRequestWithAllRequiredFieldsFilledForProvideHttpRequestBodyWhenCorrectData() {
        // given
        var preExecutionResult = preparePreExecutionResult();

        // when
        var result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result.getInstructedAmount()).extracting("amount", "currency")
                .contains("123.12", "EUR");
        assertThat(result.getDebtorAccount()).extracting("iban", "currency")
                .contains("debtorIban", "EUR");
        assertThat(result.getCreditorAccount()).extracting("iban", "currency")
                .contains("creditorIban", "EUR");
        assertThat(result.getCreditorName()).isEqualTo("Creditor");
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo("remittanceUnstructured");
    }

    @Test
    void shouldReturnInitiatePaymentRequestWithAllRequiredFieldsFilledForProvideHttpRequestBodyWhenNullDebtorAccount() {
        // given
        var preExecutionResult = preparePreExecutionResultWithNullDebtorAccount();

        // when
        var result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result.getInstructedAmount()).extracting("amount", "currency")
                .contains("123.12", "EUR");
        assertThat(result.getDebtorAccount()).isNull();
        assertThat(result.getCreditorAccount()).extracting("iban", "currency")
                .contains("creditorIban", "EUR");
        assertThat(result.getCreditorName()).isEqualTo("Creditor");
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo("remittanceUnstructured");
    }

    private CbiGlobeSepaInitiatePreExecutionResult preparePreExecutionResult() {
        return new CbiGlobeSepaInitiatePreExecutionResult(
                SepaInitiatePaymentRequestDTO.builder()
                        .instructedAmount(SepaAmountDTO.builder()
                                .amount(new BigDecimal("123.12"))
                                .build())
                        .debtorAccount(SepaAccountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .iban("debtorIban")
                                .build())
                        .creditorAccount(SepaAccountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .iban("creditorIban")
                                .build())
                        .creditorName("Creditor")
                        .remittanceInformationUnstructured("remittanceUnstructured")
                        .build(),
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    private CbiGlobeSepaInitiatePreExecutionResult preparePreExecutionResultWithNullDebtorAccount() {
        return new CbiGlobeSepaInitiatePreExecutionResult(
                SepaInitiatePaymentRequestDTO.builder()
                        .instructedAmount(SepaAmountDTO.builder()
                                .amount(new BigDecimal("123.12"))
                                .build())
                        .creditorAccount(SepaAccountDTO.builder()
                                .currency(CurrencyCode.EUR)
                                .iban("creditorIban")
                                .build())
                        .creditorName("Creditor")
                        .remittanceInformationUnstructured("remittanceUnstructured")
                        .build(),
                null,
                null,
                "",
                "",
                null,
                null,
                null
        );
    }
}
package com.yolt.providers.volksbank.common.pis.pec.initiate;

import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import com.yolt.providers.volksbank.dto.v1_1.AccountReference;
import com.yolt.providers.volksbank.dto.v1_1.Amount;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class VolksbankInitiatePaymentHttpRequestBodyProviderV2Test {

    @InjectMocks
    private VolksbankInitiatePaymentHttpRequestBodyProviderV2 subject;

    @Test
    void shouldReturnInitiatePaymentRequestWithAllRequiredFieldsFilledForProvideHttpRequestBodyWhenCorrectData() {
        // given
        var preExecutionResult = preparePreExecutionResult();

        // when
        var result = subject.provideHttpRequestBody(preExecutionResult);

        // then
        assertThat(result.getInstructedAmount()).extracting(Amount::getAmount,
                        Amount::getCurrency)
                .contains("123.12", Amount.CurrencyEnum.EUR);
        assertThat(result.getDebtorAccount()).extracting(AccountReference::getIban,
                        AccountReference::getCurrency)
                .contains("debtorIban", AccountReference.CurrencyEnum.EUR);
        assertThat(result.getCreditorAccount()).extracting(AccountReference::getIban,
                        AccountReference::getCurrency)
                .contains("creditorIban", AccountReference.CurrencyEnum.EUR);
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
        assertThat(result.getInstructedAmount()).extracting(Amount::getAmount,
                        Amount::getCurrency)
                .contains("123.12", Amount.CurrencyEnum.EUR);
        assertThat(result.getDebtorAccount()).isNull();
        assertThat(result.getCreditorAccount()).extracting(AccountReference::getIban,
                        AccountReference::getCurrency)
                .contains("creditorIban", AccountReference.CurrencyEnum.EUR);
        assertThat(result.getCreditorName()).isEqualTo("Creditor");
        assertThat(result.getRemittanceInformationUnstructured()).isEqualTo("remittanceUnstructured");
    }

    private VolksbankSepaInitiatePreExecutionResult preparePreExecutionResult() {
        return new VolksbankSepaInitiatePreExecutionResult(
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
                "",
                "",
                ""
        );
    }

    private VolksbankSepaInitiatePreExecutionResult preparePreExecutionResultWithNullDebtorAccount() {
        return new VolksbankSepaInitiatePreExecutionResult(
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
                ""
        );
    }
}
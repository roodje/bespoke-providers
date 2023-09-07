package com.yolt.providers.cbiglobe.posteitaliane.pec.initiate;

import com.yolt.providers.cbiglobe.common.exception.PaymentFailedException;
import com.yolt.providers.cbiglobe.common.pis.pec.initiate.CbiGlobeSepaInitiatePreExecutionResult;
import com.yolt.providers.cbiglobe.posteitaliane.pis.pec.initiate.PosteItalianeInitiatePaymentHttpRequestBodyProvider;
import com.yolt.providers.common.pis.sepa.DynamicFields;
import com.yolt.providers.common.pis.sepa.SepaAccountDTO;
import com.yolt.providers.common.pis.sepa.SepaAmountDTO;
import com.yolt.providers.common.pis.sepa.SepaInitiatePaymentRequestDTO;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PosteItalianeInitiatePaymentHttpRequestBodyProviderTest {

    @InjectMocks
    private PosteItalianeInitiatePaymentHttpRequestBodyProvider subject;

    @Test
    void shouldReturnInitiatePaymentRequestWithAllRequiredFieldsFilledForProvideHttpRequestBodyWhenCorrectData() {
        // given
        var preExecutionResult = preparePreExecutionResultWithDebtor();

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
        assertThat(result.getCreditorAddress().getCountry()).isEqualTo("NL");
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
        assertThat(result.getCreditorAddress().getCountry()).isEqualTo("NL");
    }

    @Test
    void shouldReturnInitiatePaymentRequestWithAllRequiredFieldsFilledForProvideHttpRequestBodyWhenNullCreditorPostalCountry() {
        // given
        var preExecutionResult = preparePreExecutionResultWithNullCreditorPostalCountry();

        // when-then
        assertThatThrownBy(() -> subject.provideHttpRequestBody(preExecutionResult))
                .isExactlyInstanceOf(PaymentFailedException.class)
                .hasMessage("Missing creditor postal country within the request");
    }

    @Test
    void shouldReturnInitiatePaymentRequestWithAllRequiredFieldsFilledForProvideHttpRequestBodyWhenUnsupportedCurrencyInCreditorAccount() {
        // given
        var preExecutionResult = preparePreExecutionResultWithUnsupportedCurrencyInCreditorAccount();

        // when-then
        assertThatThrownBy(() -> subject.provideHttpRequestBody(preExecutionResult))
                .isExactlyInstanceOf(PaymentFailedException.class)
                .hasMessage("Currency used in payment request for Creditor or Debtor field is not allowed: PLN");
    }

    private CbiGlobeSepaInitiatePreExecutionResult preparePreExecutionResultWithDebtor() {
        var dynamicFields = new DynamicFields();
        dynamicFields.setCreditorPostalCountry("NL");
        var debtor = SepaAccountDTO.builder()
                .currency(CurrencyCode.EUR)
                .iban("debtorIban")
                .build();
        return preparePreExecutionResult(dynamicFields, CurrencyCode.EUR, debtor);
    }

    private CbiGlobeSepaInitiatePreExecutionResult preparePreExecutionResultWithNullDebtorAccount() {
        var dynamicFields = new DynamicFields();
        dynamicFields.setCreditorPostalCountry("NL");
        return preparePreExecutionResult(dynamicFields, CurrencyCode.EUR, null);
    }

    private CbiGlobeSepaInitiatePreExecutionResult preparePreExecutionResultWithNullCreditorPostalCountry() {
        return preparePreExecutionResult(null, CurrencyCode.EUR, null);
    }

    private CbiGlobeSepaInitiatePreExecutionResult preparePreExecutionResultWithUnsupportedCurrencyInCreditorAccount() {
        var dynamicFields = new DynamicFields();
        dynamicFields.setCreditorPostalCountry("NL");
        return preparePreExecutionResult(dynamicFields, CurrencyCode.PLN, null);
    }

    private CbiGlobeSepaInitiatePreExecutionResult preparePreExecutionResult(DynamicFields dynamicFields,
                                                                             CurrencyCode currencyCode,
                                                                             SepaAccountDTO debtorAccount) {
        return new CbiGlobeSepaInitiatePreExecutionResult(
                SepaInitiatePaymentRequestDTO.builder()
                        .instructedAmount(SepaAmountDTO.builder()
                                .amount(new BigDecimal("123.12"))
                                .build())
                        .debtorAccount(debtorAccount)
                        .creditorAccount(SepaAccountDTO.builder()
                                .currency(currencyCode)
                                .iban("creditorIban")
                                .build())
                        .creditorName("Creditor")
                        .remittanceInformationUnstructured("remittanceUnstructured")
                        .dynamicFields(dynamicFields)
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
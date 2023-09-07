package com.yolt.providers.starlingbank.common.paymentexecutioncontext;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.starlingbank.common.model.*;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.model.StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult;
import com.yolt.providers.starlingbank.common.paymentexecutioncontext.provider.StarlingBankSubmitPaymentHttpRequestBodyProvider;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class StarlingBankUkPaymentSubmitPaymentHttpRequestBodyProviderTest {

    @InjectMocks
    private StarlingBankSubmitPaymentHttpRequestBodyProvider httpRequestBodyProvider;

    @Test
    void shouldReturnConfirmPaymentRequestForCreateWithCorrectSubmitPaymentRequest() {
        // given
        StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult submitPaymentPreExecutionResult = StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult.builder()
                .token("token")
                .url("https://www.example.com")
                .paymentRequest(createUkPaymentRequest(AccountIdentifierScheme.SORTCODEACCOUNTNUMBER))
                .build();
        PaymentRequest expectedResult = PaymentRequest.builder()
                .externalIdentifier(UUID.randomUUID().toString())
                .paymentRecipient(PaymentRecipient.builder()
                        .payeeName("Michal Dziewanowski")
                        .payeeType(PayeeType.INDIVIDUAL)
                        .countryCode("GB")
                        .accountIdentifier("88888887")
                        .bankIdentifier("566666")
                        .bankIdentifierType(BankIdentifierType.SORT_CODE)
                        .build())
                .reference("reference")
                .amount(new CurrencyAndAmountV2("PLN", new BigDecimal("12311")))
                .build();

        // when
        PaymentRequest result = httpRequestBodyProvider.provideHttpRequestBody(submitPaymentPreExecutionResult);

        // then
        assertThat(result).usingRecursiveComparison().ignoringFields("externalIdentifier")
                .isEqualTo(expectedResult);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenAccountTypeOtherThanSortCodeAccountNumberIsProvided() {
        // given
        StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult submitPaymentPreExecutionResult = StarlingBankSubmitAndStatusPaymentExecutionContextPreExecutionResult.builder()
                .token("token")
                .url("https://www.example.com")
                .paymentRequest(createUkPaymentRequest(AccountIdentifierScheme.IBAN))
                .build();

        //when
        ThrowableAssert.ThrowingCallable call = () -> httpRequestBodyProvider.provideHttpRequestBody(submitPaymentPreExecutionResult);

        //then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(call)
                .withMessage("Invalid scheme type.");
    }

    private InitiateUkDomesticPaymentRequestDTO createUkPaymentRequest(AccountIdentifierScheme accountScheme) {
        return new InitiateUkDomesticPaymentRequestDTO(
                "endToEndIdentification",
                CurrencyCode.PLN.name(),
                new BigDecimal("123.11"),
                new UkAccountDTO(
                        "56666688888887",
                        accountScheme,
                        "Michal Dziewanowski",
                        null),
                null,
                "Payment reference unstructured",
                Collections.singletonMap("remittanceInformationStructured", "reference"));
    }
}

package com.yolt.providers.yoltprovider.pis;

import com.yolt.providers.common.exception.PaymentValidationException;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequest;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
public class YoltBankPaymentRequestBodyValidatorTest {

    private YoltBankPaymentRequestBodyValidator subject;

    @BeforeEach
    public void setup() {
        subject = new YoltBankPaymentRequestBodyValidator();
    }

    @Test
    public void shouldPassValidationIfRemittanceInformationStructuredHasCorrectValue() {
        //given
        InitiateUkDomesticPaymentRequest request = createRequestWithDynamicFieldDebtorName("CORRECT_VALUE");

        //when
        ThrowableAssert.ThrowingCallable validateCallable = () -> subject.validate(request);

        //then
        assertThatCode(validateCallable)
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldPassValidationIfRemittanceInformationStructuredIsNull() {
        //given
        InitiateUkDomesticPaymentRequest request = createRequestWithDynamicFieldDebtorName(null);

        //when
        ThrowableAssert.ThrowingCallable validateCallable = () -> subject.validate(request);

        //then
        assertThatCode(validateCallable)
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldPassValidationIfRemittanceInformationStructuredHasInCorrectValue() {
        //given
        InitiateUkDomesticPaymentRequest request = createRequestWithDynamicFieldDebtorName("INCORRECT_VALUE");

        //when
        ThrowableAssert.ThrowingCallable validateCallable = () -> subject.validate(request);

        //then
        assertThatExceptionOfType(PaymentValidationException.class)
                .isThrownBy(validateCallable)
                .withMessage("Field DYNAMICFIELDS_DEBTORNAME should match pattern: ^CORRECT_VALUE$");
    }

    private InitiateUkDomesticPaymentRequest createRequestWithDynamicFieldDebtorName(String debtorName) {
        Map<String, String> dynamicFields = debtorName == null ? emptyMap() : Map.of("debtorName", debtorName);
        InitiateUkDomesticPaymentRequest request = new InitiateUkDomesticPaymentRequest(
                new InitiateUkDomesticPaymentRequestDTO(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        dynamicFields
                ),
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        return request;
    }

}
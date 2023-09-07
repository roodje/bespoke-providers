package com.yolt.providers.openbanking.ais.aibgroup.pis.ni;

import com.yolt.providers.openbanking.ais.aibgroup.common.pec.AibNIPaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class AibNiPaymentRequestValidatorTest {

    private static final String SORT_CODE_ACCOUNT_NUMBER = "UK.OBIE.SortCodeAccountNumber";
    private static final String IBAN_ACCOUNT_NUMBER = "UK.OBIE.IBAN";
    private static final String EUR = CurrencyCode.EUR.toString();

    private AibNIPaymentRequestValidator subject = new AibNIPaymentRequestValidator();

    private static Stream<Arguments> getDtoAndExpectedResult() {
        return Stream.of(
                Arguments.of(prepareDataInitiation(false,
                        null,
                        IBAN_ACCOUNT_NUMBER,
                        "25.0",
                        EUR,
                        "Too&long#reference@information",
                        "unstructured",
                        "END123"), "Remittance information 'Reference' is too long. Maximum allowed length is 18"),
                Arguments.of(prepareDataInitiation(false,
                        null,
                        IBAN_ACCOUNT_NUMBER,
                        "25.0",
                        EUR,
                        "reference",
                        "Too&long#unstructured#reference@information#Too&long#unstructured#reference@information",
                        "END123"), "Remittance information 'Unstructured' is too long. Maximum allowed length is 70"),
                Arguments.of(prepareDataInitiation(false,
                        null,
                        IBAN_ACCOUNT_NUMBER,
                        "25.0",
                        EUR,
                        "reference",
                        "unstructured",
                        ""), "EndToEndIdentification is required. Maximum allowed length is 30"),
                Arguments.of(prepareDataInitiation(false,
                        null,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        EUR,
                        "reference",
                        "unstructured",
                        "END123"), "Creditor name, account scheme(IBAN) and number are required"));
    }

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectRequestBodyIsProvided() {
        //given
        OBWriteDomestic2DataInitiation dataInitiation = prepareDataInitiation(true,
                IBAN_ACCOUNT_NUMBER,
                IBAN_ACCOUNT_NUMBER,
                "25.0",
                EUR,
                "reference",
                "unstructured",
                "END1234");

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validateRequest(dataInitiation);

        //then
        assertThatCode(call).doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectRequestBodyWithoutDebtorAccountIsProvided() {
        //given
        OBWriteDomestic2DataInitiation dataInitiation = prepareDataInitiation(false,
                IBAN_ACCOUNT_NUMBER,
                IBAN_ACCOUNT_NUMBER,
                "25.0",
                EUR,
                "reference",
                "unstructured",
                "END123");

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validateRequest(dataInitiation);

        //then
        assertThatCode(call).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getDtoAndExpectedResult")
    void shouldThrowIllegalArgumentExceptionWhenProvidedDtoDoesNotPassValidation(OBWriteDomestic2DataInitiation givenDataInitiation, String expectedErrorMessage) {
        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validateRequest(givenDataInitiation);

        //then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(call)
                .withMessage(expectedErrorMessage);
    }

    private static OBWriteDomestic2DataInitiation prepareDataInitiation(boolean withDebtorAccount,
                                                                        String debtorScheme,
                                                                        String creditorScheme,
                                                                        String amount,
                                                                        String currency,
                                                                        String remittanceInformationReference,
                                                                        String remittanceInformationUnstructured,
                                                                        String endToEndIdentification) {
        return new OBWriteDomestic2DataInitiation()
                .creditorAccount(new OBWriteDomestic2DataInitiationCreditorAccount()
                        .schemeName(creditorScheme)
                        .identification("200052 75849855")
                        .name("TestUser"))
                .debtorAccount(withDebtorAccount ? new OBWriteDomestic2DataInitiationDebtorAccount()
                        .schemeName(debtorScheme)
                        .identification("309493 01273801") : null)
                .instructionIdentification("Instruction identifier")
                .endToEndIdentification(endToEndIdentification)
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount(amount)
                        .currency(currency))
                .localInstrument("localInstrument")
                .remittanceInformation(new OBWriteDomestic2DataInitiationRemittanceInformation()
                        .unstructured(remittanceInformationUnstructured)
                        .reference(remittanceInformationReference));
    }
}
package com.yolt.providers.openbanking.ais.bankofirelandgroup.common.service.pis;

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
class BankOfIrelandGroupPaymentRequestValidatorTest {

    private static final String SORT_CODE_ACCOUNT_NUMBER = "UK.OBIE.SortCodeAccountNumber";
    private static final String GBP = CurrencyCode.GBP.toString();

    private BankOfIrelandGroupPaymentRequestValidator subject = new BankOfIrelandGroupPaymentRequestValidator();

    private static Stream<Arguments> getDtoAndExpectedResult() {
        return Stream.of(
                Arguments.of(prepareDataInitiation(false,
                        null,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        GBP,
                        "Too&long#reference@information",
                        "END123"), "Remittance information is too long. Maximum allowed length is 18"),
                Arguments.of(prepareDataInitiation(false,
                        null,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        GBP,
                        "Remittance Info",
                        ""), "End to end identification is required"),
                Arguments.of(prepareDataInitiation(false,
                        null,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        null,
                        "Remittance Info",
                        "END123"), "Amount and currency are required"),
                Arguments.of(prepareDataInitiation(false,
                        null,
                        null,
                        "25.0",
                        GBP,
                        "Remittance Info",
                        "END123"), "Creditor name, account scheme and number are required"),
                Arguments.of(prepareDataInitiation(true,
                        null,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        GBP,
                        "Remittance Info",
                        "END123"), "Debtor account scheme and number are required, when debtor data is provided")
        );
    }

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectRequestBodyIsProvided() {
        //given
        OBWriteDomestic2DataInitiation dataInitiation = prepareDataInitiation(true,
                SORT_CODE_ACCOUNT_NUMBER,
                SORT_CODE_ACCOUNT_NUMBER,
                "25.0",
                GBP,
                "Remittance Info",
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
                SORT_CODE_ACCOUNT_NUMBER,
                SORT_CODE_ACCOUNT_NUMBER,
                "25.0",
                GBP,
                "Remittance Info",
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
                                                                        String remittanceInformation, String endToEndIdentification) {
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
                        .unstructured("unstructured")
                        .reference(remittanceInformation));
    }

}
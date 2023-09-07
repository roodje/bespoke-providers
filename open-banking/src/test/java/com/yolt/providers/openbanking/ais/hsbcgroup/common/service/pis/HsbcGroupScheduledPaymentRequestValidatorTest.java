package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.pis;

import com.yolt.providers.openbanking.dto.pis.openbanking316.*;
import nl.ing.lovebird.extendeddata.common.CurrencyCode;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(MockitoExtension.class)
class HsbcGroupScheduledPaymentRequestValidatorTest {

    private static final String SORT_CODE_ACCOUNT_NUMBER = "UK.OBIE.SortCodeAccountNumber";
    private static final String IBAN = "UK.OBIE.IBAN";
    private static final String GBP = CurrencyCode.GBP.toString();
    private static final String PLN = CurrencyCode.PLN.toString();

    private Clock clock = Clock.systemUTC();
    private HsbcGroupScheduledPaymentRequestValidator subject = new HsbcGroupScheduledPaymentRequestValidator(clock);

    private static Stream<Arguments> getDtoAndExpectedResult() {
        return Stream.of(
                Arguments.of(prepareDataInitiation(true,
                        OffsetDateTime.now().plusDays(10),
                        SORT_CODE_ACCOUNT_NUMBER,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        GBP,
                        "Too&long#reference@information"), "Remittance information contains not allowed characters. It should match ^[A-Za-z0-9\\/\\s\\.\\+\\:\\(,\\&\\)-?]{1,18}$"),
                Arguments.of(prepareDataInitiation(true,
                        null,
                        SORT_CODE_ACCOUNT_NUMBER,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        GBP,
                        "Remittance Info"), "Execution date is required"),
                Arguments.of(prepareDataInitiation(true,
                        OffsetDateTime.now().minusDays(10),
                        SORT_CODE_ACCOUNT_NUMBER,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        GBP,
                        "Remittance Info"), "Execution date for scheduled payment can't be in past"),
                Arguments.of(prepareDataInitiation(true,
                        OffsetDateTime.now().plusYears(1).plusDays(10),
                        SORT_CODE_ACCOUNT_NUMBER,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        GBP,
                        "Remittance Info"), "Execution date for scheduled payment can't be more than one year ahead"),
                Arguments.of(prepareDataInitiation(true,
                        OffsetDateTime.now().plusDays(10),
                        IBAN,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "25.0",
                        GBP,
                        "Remittance Info"), "Only UK.OBIE.SortCodeAccountNumber is supported as debtor scheme"),
                Arguments.of(prepareDataInitiation(true,
                        OffsetDateTime.now().plusDays(10),
                        SORT_CODE_ACCOUNT_NUMBER,
                        IBAN,
                        "25.0",
                        GBP,
                        "Remittance Info"), "Only UK.OBIE.SortCodeAccountNumber is supported as creditor scheme"),
                Arguments.of(prepareDataInitiation(true,
                        OffsetDateTime.now().plusDays(10),
                        SORT_CODE_ACCOUNT_NUMBER,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "",
                        GBP,
                        "Remittance Info"), "Amount is required"),
                Arguments.of(prepareDataInitiation(true,
                        OffsetDateTime.now().plusDays(10),
                        SORT_CODE_ACCOUNT_NUMBER,
                        SORT_CODE_ACCOUNT_NUMBER,
                        "26.0",
                        PLN,
                        "Remittance Info"), "Only GBP is supported currency")

        );
    }

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectRequestBodyIsProvided() {
        //given
        OBWriteDomesticScheduled2DataInitiation dataInitiation = prepareDataInitiation(true,
                OffsetDateTime.now().plusDays(10),
                SORT_CODE_ACCOUNT_NUMBER,
                SORT_CODE_ACCOUNT_NUMBER,
                "25.0",
                GBP,
                "Remittance Info");

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validateRequest(dataInitiation);

        //then
        assertThatCode(call).doesNotThrowAnyException();
    }

    @Test
    void shouldNotThrowAnyExceptionWhenCorrectRequestBodyWithoutDebtorAccountIsProvided() {
        //given
        OBWriteDomesticScheduled2DataInitiation dataInitiation = prepareDataInitiation(false, OffsetDateTime.now().plusDays(10),
                SORT_CODE_ACCOUNT_NUMBER,
                SORT_CODE_ACCOUNT_NUMBER,
                "25.0",
                GBP,
                "Remittance Info");

        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validateRequest(dataInitiation);

        //then
        assertThatCode(call).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getDtoAndExpectedResult")
    void shouldThrowIllegalArgumentExceptionWhenProvidedDtoDoesNotPassValidation(OBWriteDomesticScheduled2DataInitiation givenDataInitiation, String expectedErrorMessage) {
        //when
        ThrowableAssert.ThrowingCallable call = () -> subject.validateRequest(givenDataInitiation);

        //then
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(call)
                .withMessage(expectedErrorMessage);
    }

    private static OBWriteDomesticScheduled2DataInitiation prepareDataInitiation(boolean withDebtorAccount,
                                                                                 OffsetDateTime requestedExecutionDate,
                                                                                 String debtorScheme,
                                                                                 String creditorScheme,
                                                                                 String amount,
                                                                                 String currency,
                                                                                 String remittanceInformation) {
        return new OBWriteDomesticScheduled2DataInitiation()
                .creditorAccount(new OBWriteDomestic2DataInitiationCreditorAccount()
                        .schemeName(creditorScheme)
                        .identification("200052 75849855"))
                .debtorAccount(withDebtorAccount ? new OBWriteDomestic2DataInitiationDebtorAccount()
                        .schemeName(debtorScheme)
                        .identification("309493 01273801") : null)
                .instructionIdentification("Instruction identifier")
                .endToEndIdentification("end-to-end")
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount(amount)
                        .currency(currency))
                .localInstrument("localInstrument")
                .remittanceInformation(new OBWriteDomestic2DataInitiationRemittanceInformation()
                        .unstructured("unstructured")
                        .reference(remittanceInformation))
                .requestedExecutionDateTime(requestedExecutionDate);
    }

}
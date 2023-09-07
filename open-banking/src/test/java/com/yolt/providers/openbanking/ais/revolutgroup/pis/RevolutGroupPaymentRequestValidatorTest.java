package com.yolt.providers.openbanking.ais.revolutgroup.pis;

import com.yolt.providers.openbanking.ais.revolutgroup.common.pec.RevolutPaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationCreditorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationInstructedAmount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationRemittanceInformation;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class RevolutGroupPaymentRequestValidatorTest {

    private RevolutPaymentRequestValidator validator = new RevolutPaymentRequestValidator();

    @Test
    void shouldPassTheValidation() {
        // given
        OBWriteDomestic2DataInitiation request = createValidRequest();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> validator.validateRequest(request);

        // then
        assertThatNoException().isThrownBy(throwingCallable);
    }

    @ParameterizedTest
    @MethodSource("getInvalidDTOsWithExpectedErrorMessage")
    void shouldThrowIllegalArgumentExceptionWithAppropriateMessageWhenTheValidationFails(OBWriteDomestic2DataInitiation request, String exceptionMessage) {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> validator.validateRequest(request);

        // then
        assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage(exceptionMessage);
    }

    private static OBWriteDomestic2DataInitiation createValidRequest() {
        return new OBWriteDomestic2DataInitiation()
                .instructionIdentification("123123123123123")
                .endToEndIdentification("123123123123123")
                .remittanceInformation(new OBWriteDomestic2DataInitiationRemittanceInformation().unstructured("unstructured"))
                .creditorAccount(new OBWriteDomestic2DataInitiationCreditorAccount()
                        .identification("8272908780568576")
                        .name("Alex Mitchell")
                        .schemeName("UK.OBIE.SortCodeAccountNumber"))
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("0.01")
                        .currency("GBP"));
    }

    private static Stream<Arguments> getInvalidDTOsWithExpectedErrorMessage() {


        OBWriteDomestic2DataInitiation invalidEndToEndId = createValidRequest().endToEndIdentification("");
        OBWriteDomestic2DataInitiation invalidAmount = createValidRequest().instructedAmount(
                new OBWriteDomestic2DataInitiationInstructedAmount()
                        .currency("PLN")
                        .amount("fifteen"));
        OBWriteDomestic2DataInitiation invalidInstructionId = createValidRequest()
                .instructionIdentification("TOOLONG123TOOLONG123TOOLONG123TOOLONG123TOOLONG123TOOLONG123");
        OBWriteDomestic2DataInitiation invalidCreditorAccountName = createValidRequest();
        invalidCreditorAccountName.getCreditorAccount().setName(null);
        OBWriteDomestic2DataInitiation invalidAccIdentification = createValidRequest();
        invalidAccIdentification.getCreditorAccount().identification(null);

        return Stream.of(
                Arguments.of(invalidEndToEndId, "Illegal end-to-end identification provided. It should match the following pattern: ^.{1,35}$"),
                Arguments.of(invalidAmount, "Illegal payment amount provided. It should match the following pattern: ^\\d{1,13}$|^\\d{1,13}\\.\\d{1,5}$"),
                Arguments.of(invalidInstructionId, "Illegal instruction identification provided. It should match the following pattern: ^.{1,35}$"),
                Arguments.of(invalidCreditorAccountName, "Illegal creditor name provided. It should match the following pattern: ^.{1,70}$"),
                Arguments.of(invalidAccIdentification, "Illegal account identification provided. It should match the following pattern: ^\\S{1,256}$")
        );
    }
}
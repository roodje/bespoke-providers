package com.yolt.providers.openbanking.ais.tidegroup.pis.validator;

import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationCreditorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationInstructedAmount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationRemittanceInformation;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class TidePaymentRequestValidatorTest {

    private static String VALUE_NOT_MATCHING_REGEXP = """
            abcdefghijklmnopqrstuwxyzabcdefghijklmnopqrstuwxyzabcdefghijklmnopqrstuwxyz
            abcdefghijklmnopqrstuwxyzabcdefghijklmnopqrstuwxyzabcdefghijklmnopqrstuwxyz
            abcdefghijklmnopqrstuwxyzabcdefghijklmnopqrstuwxyzabcdefghijklmnopqrstuwxyz
            abcdefghijklmnopqrstuwxyzabcdefghijklmnopqrstuwxyzabcdefghijklmnopqrstuwxyz""";
    private final TidePaymentRequestValidator subject = new TidePaymentRequestValidator();

    @Test
    void shouldPassTheValidation() {
        // given
        OBWriteDomestic2DataInitiation request = createValidRequest();

        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> subject.validateRequest(request);

        // then
        assertThatNoException().isThrownBy(throwingCallable);
    }

    @ParameterizedTest
    @MethodSource("getInvalidDTOsWithExpectedErrorMessage")
    void shouldThrowIllegalArgumentExceptionWithAppropriateMessageWhenTheValidationFails(OBWriteDomestic2DataInitiation request, String exceptionMessage) {
        // when
        ThrowableAssert.ThrowingCallable throwingCallable = () -> subject.validateRequest(request);

        // then
        assertThatThrownBy(throwingCallable)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessage(exceptionMessage);
    }

    private static OBWriteDomestic2DataInitiation createValidRequest() {
        return new OBWriteDomestic2DataInitiation()
                .instructionIdentification("123123123123123")
                .endToEndIdentification("123123123123123")
                .remittanceInformation(validRemittanceInformation())
                .creditorAccount(validCreditorAccount())
                .instructedAmount(validAmount());
    }

    private static OBWriteDomestic2DataInitiationRemittanceInformation validRemittanceInformation() {
        return new OBWriteDomestic2DataInitiationRemittanceInformation()
                .unstructured("unstructured")
                .reference("reference");
    }

    private static OBWriteDomestic2DataInitiationInstructedAmount validAmount() {
        return new OBWriteDomestic2DataInitiationInstructedAmount()
                .amount("0.01")
                .currency("GBP");
    }

    private static OBWriteDomestic2DataInitiationCreditorAccount validCreditorAccount() {
        return new OBWriteDomestic2DataInitiationCreditorAccount()
                .identification("8272908780568576")
                .name("Alex Mitchell")
                .schemeName("UK.OBIE.SortCodeAccountNumber");
    }

    private static Stream<Arguments> getInvalidDTOsWithExpectedErrorMessage() {
        OBWriteDomestic2DataInitiation emptyInstructionId = createValidRequest()
                .instructionIdentification("");
        OBWriteDomestic2DataInitiation notMatchingRegexpInstructionId = createValidRequest()
                .instructionIdentification(VALUE_NOT_MATCHING_REGEXP);
        OBWriteDomestic2DataInitiation emptyEndToEndId = createValidRequest().endToEndIdentification("");
        OBWriteDomestic2DataInitiation notMatchingRegexpEndToEndId = createValidRequest().endToEndIdentification(VALUE_NOT_MATCHING_REGEXP);
        OBWriteDomestic2DataInitiation nullInstructedAmount = createValidRequest().instructedAmount(null);
        OBWriteDomestic2DataInitiation nullAmount = createValidRequest().instructedAmount(
                validAmount().amount(null));
        OBWriteDomestic2DataInitiation notMatchingRegexpAmount = createValidRequest().instructedAmount(
                validAmount().amount(VALUE_NOT_MATCHING_REGEXP));
        OBWriteDomestic2DataInitiation nullCurrency = createValidRequest().instructedAmount(
                validAmount().currency(null));
        OBWriteDomestic2DataInitiation notMatchingRegexpCurrency = createValidRequest().instructedAmount(
                validAmount().currency(VALUE_NOT_MATCHING_REGEXP));
        OBWriteDomestic2DataInitiation nullName = createValidRequest().creditorAccount(
                validCreditorAccount().name(null));
        OBWriteDomestic2DataInitiation notMatchingRegexpName = createValidRequest().creditorAccount(
                validCreditorAccount().name(VALUE_NOT_MATCHING_REGEXP));
        OBWriteDomestic2DataInitiation nullScheme = createValidRequest().creditorAccount(
                validCreditorAccount().schemeName(null));
        OBWriteDomestic2DataInitiation notMatchingRegexpScheme = createValidRequest().creditorAccount(
                validCreditorAccount().schemeName(VALUE_NOT_MATCHING_REGEXP));
        OBWriteDomestic2DataInitiation nullAccountIdentification = createValidRequest().creditorAccount(
                validCreditorAccount().identification(null));
        OBWriteDomestic2DataInitiation notMatchingRegexpAccountIdentification = createValidRequest().creditorAccount(
                validCreditorAccount().identification(VALUE_NOT_MATCHING_REGEXP));
        OBWriteDomestic2DataInitiation notMatchingRegexpRemittanceInformationUnstructured = createValidRequest().remittanceInformation(
                validRemittanceInformation().unstructured(VALUE_NOT_MATCHING_REGEXP));
        OBWriteDomestic2DataInitiation nullReference = createValidRequest().remittanceInformation(
                validRemittanceInformation().reference(null));
        OBWriteDomestic2DataInitiation notMatchingRegexpReference = createValidRequest().remittanceInformation(
                validRemittanceInformation().reference(VALUE_NOT_MATCHING_REGEXP));
        OBWriteDomestic2DataInitiation noContiguous6AlphanumericCharactersReference = createValidRequest().remittanceInformation(
                validRemittanceInformation().reference("ref---ref"));
        OBWriteDomestic2DataInitiation onlyOneKindOfAlphanumericCharacterReference = createValidRequest().remittanceInformation(
                validRemittanceInformation().reference("...aaaaaa aaa&&.-aaa"));

        return Stream.of(
                Arguments.of(emptyInstructionId, "Illegal instruction identification provided. It should match the following pattern: ^[a-zA-Z0-9/?:().,+ #=!-]{1,35}$"),
                Arguments.of(notMatchingRegexpInstructionId, "Illegal instruction identification provided. It should match the following pattern: ^[a-zA-Z0-9/?:().,+ #=!-]{1,35}$"),
                Arguments.of(emptyEndToEndId, "Illegal end-to-end identification provided. It should match the following pattern: ^.{1,31}$"),
                Arguments.of(notMatchingRegexpEndToEndId, "Illegal end-to-end identification provided. It should match the following pattern: ^.{1,31}$"),
                Arguments.of(nullInstructedAmount, "Illegal payment amount provided. It should match the following pattern: ^\\d{1,13}\\.\\d{1,5}$"),
                Arguments.of(nullAmount, "Illegal payment amount provided. It should match the following pattern: ^\\d{1,13}\\.\\d{1,5}$"),
                Arguments.of(notMatchingRegexpAmount, "Illegal payment amount provided. It should match the following pattern: ^\\d{1,13}\\.\\d{1,5}$"),
                Arguments.of(nullCurrency, "Illegal currency value provided. It should be set to GBP"),
                Arguments.of(notMatchingRegexpCurrency, "Illegal currency value provided. It should be set to GBP"),
                Arguments.of(nullName, "Illegal creditor name provided. It should match the following pattern: ^.{1,70}$"),
                Arguments.of(notMatchingRegexpName, "Illegal creditor name provided. It should match the following pattern: ^.{1,70}$"),
                Arguments.of(nullScheme, "Illegal scheme name provided. It should be: UK.OBIE.SortCodeAccountNumber"),
                Arguments.of(notMatchingRegexpScheme, "Illegal scheme name provided. It should be: UK.OBIE.SortCodeAccountNumber"),
                Arguments.of(nullAccountIdentification, "Illegal account identification provided. It should match the following pattern: ^\\d{1,256}$"),
                Arguments.of(notMatchingRegexpAccountIdentification, "Illegal account identification provided. It should match the following pattern: ^\\d{1,256}$"),
                Arguments.of(notMatchingRegexpRemittanceInformationUnstructured, "Illegal remittance info unstructured provided. It should match the following pattern: ^.{1,140}$"),
                Arguments.of(nullReference, "Illegal remittance info structured provided. It should match the following pattern: ^[A-Za-z0-9 &\\-./]{6,35}$"),
                Arguments.of(notMatchingRegexpReference, "Illegal remittance info structured provided. It should match the following pattern: ^[A-Za-z0-9 &\\-./]{6,35}$"),
                Arguments.of(noContiguous6AlphanumericCharactersReference, "Illegal remittance info structured provided. Must contain a contiguous string of at least 6 alphanumeric characters"),
                Arguments.of(onlyOneKindOfAlphanumericCharacterReference, "Illegal remittance info structured provided. After stripping out non-alphanumeric characters the resulting string cannot consist of all the same character")
        );
    }
}
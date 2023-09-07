package com.yolt.providers.openbanking.ais.hsbcgroup.common.service.pis;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationRemittanceInformation;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HsbcGroupPaymentRequestValidatorTest {

    private OBWriteDomestic2DataInitiation dataInitiation;

    private PaymentRequestValidator paymentRequestValidator = new HsbcGroupPaymentRequestValidator();

    private List<String> getCorrectReferences() {
        return List.of("aXa-?-:(bbb)+1/3.,", " 0132&");
    }

    private List<String> getIncorrectReferences() {
        return List.of("###", "{}", "this is reference longer than 18 characters", "");
    }

    @ParameterizedTest
    @MethodSource("getCorrectReferences")
    void shouldNotThrowAnyExceptionWhenReferenceMatchesPattern(String testReference) {
        //Given
        dataInitiation = getDataInitialization(testReference);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);
        //Than

        assertThatCode(callable).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource({"getIncorrectReferences"})
    void shouldThrowExceptionWhenReferenceDoesntMatchPattern(String testReference) {
        //Given
        dataInitiation = getDataInitialization(testReference);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);
        //Than
        assertThatCode(callable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Remittance information contains not allowed characters. It should match ^[A-Za-z0-9\\/\\s\\.\\+\\:\\(,\\&\\)-?]{1,18}$");
    }

    @Test
    void shouldThrowExceptionWhenReferenceIsNull() {
        //Given
        dataInitiation = getDataInitialization(null);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);
        //Than

        assertThatCode(callable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Remittance information contains not allowed characters. It should match ^[A-Za-z0-9\\/\\s\\.\\+\\:\\(,\\&\\)-?]{1,18}$");
    }

    private OBWriteDomestic2DataInitiation getDataInitialization(String reference) {

        OBWriteDomestic2DataInitiationRemittanceInformation remittanceInformation = new OBWriteDomestic2DataInitiationRemittanceInformation();
        remittanceInformation.setReference(reference);
        OBWriteDomestic2DataInitiation result = new OBWriteDomestic2DataInitiation();
        result.setRemittanceInformation(remittanceInformation);

        return result;
    }


}

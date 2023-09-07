package com.yolt.providers.openbanking.ais.monzogroup.pis;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.ais.monzogroup.common.service.pis.MonzoGroupPaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MonzoPaymentRequestValidatorTest {

    private OBWriteDomestic2DataInitiation dataInitiation;
    private PaymentRequestValidator paymentRequestValidator = new MonzoGroupPaymentRequestValidator();

    private List<String> getCorrectReferences() {
        return List.of("aXa-?-:(bbb)+1/3.,", " 0132&");
    }

    private List<String> getIncorrectReferences() {
        return List.of("€", "[]", "this is reference longer than 18 characters", "");
    }

    @ParameterizedTest
    @MethodSource("getCorrectReferences")
    public void shouldNotThrowAnyExceptionWhenReferenceMatchesPattern(String testReference) {
        //Given
        dataInitiation = getDataInitialization(testReference);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);
        //Than
        assertThatCode(callable).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource({"getIncorrectReferences"})
    public void shouldThrowExceptionWhenReferenceDoesntMatchPattern(String testReference) {
        //Given
        dataInitiation = getDataInitialization(testReference);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);
        //Than
        assertThatCode(callable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Remittance information contains not allowed characters. It should match ^[A-Za-z0-9\\/\\-?:().,'+ #=!\"%&*<>;{}@\\r\\n]{1,18}$");
    }

    @Test
    public void shouldThrowExceptionWhenReferenceIsNull() {
        //Given
        dataInitiation = getDataInitialization(null);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);
        //Than

        assertThatCode(callable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Remittance information contains not allowed characters. It should match ^[A-Za-z0-9\\/\\-?:().,'+ #=!\"%&*<>;{}@\\r\\n]{1,18}$");
    }

    private OBWriteDomestic2DataInitiation getDataInitialization(String reference) {
        OBWriteDomestic2DataInitiationCreditorAccount creditorAccount = new OBWriteDomestic2DataInitiationCreditorAccount();
        creditorAccount.setName("creditorName");
        creditorAccount.setSchemeName("UK.OBIE.SortCodeAccountNumber");
        OBWriteDomestic2DataInitiationDebtorAccount debtorAccount = new OBWriteDomestic2DataInitiationDebtorAccount();
        debtorAccount.setName("debtorName");
        debtorAccount.setSchemeName("UK.OBIE.SortCodeAccountNumber");
        OBWriteDomestic2DataInitiationRemittanceInformation remittanceInformation = new OBWriteDomestic2DataInitiationRemittanceInformation();
        remittanceInformation.setReference(reference);
        OBWriteDomestic2DataInitiation result = new OBWriteDomestic2DataInitiation();
        OBWriteDomestic2DataInitiationInstructedAmount amount = new OBWriteDomestic2DataInitiationInstructedAmount();
        amount.setCurrency("GBP");
        result.setRemittanceInformation(remittanceInformation);
        result.setCreditorAccount(creditorAccount);
        result.setInstructedAmount(amount);

        return result;
    }


}

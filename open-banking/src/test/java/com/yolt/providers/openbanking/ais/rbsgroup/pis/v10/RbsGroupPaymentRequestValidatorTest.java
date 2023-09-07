package com.yolt.providers.openbanking.ais.rbsgroup.pis.v10;

import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.ais.rbsgroup.common.pec.mapper.validator.RbsGroupPaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationCreditorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationDebtorAccount;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiationRemittanceInformation;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class RbsGroupPaymentRequestValidatorTest {

    private OBWriteDomestic2DataInitiation dataInitiation;
    private PaymentRequestValidator paymentRequestValidator = new RbsGroupPaymentRequestValidator();

    private List<String> getCorrectReferences() {
        return List.of("aXa-?-:'+ZZ.,() +'", "0123");

    }

    private List<String> getIncorrectReferences() {
        return List.of("##'", "!",
                "This is too long value:567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
    }

    @ParameterizedTest
    @MethodSource("getCorrectReferences")
    public void shouldNotThrowAnyExceptionWhenReferenceMatchesPattern(String testReference) {
        //Given
        dataInitiation = getDataInitialization(testReference);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);
        //Then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenReferenceIsNull() {
        //Given
        dataInitiation = getDataInitialization(null);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);
        //Then
        assertThatCode(callable)
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getIncorrectReferences")
    public void shouldThrowExceptionWhenReferenceDoesntMatchPattern(String testReference) {
        //Given
        dataInitiation = getDataInitialization(testReference);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);

        //Than

        assertThatCode(callable)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Remittance information contains not allowed characters. It should match " + "^[A-Za-z0-9\\/\\s\\.\\+\\:\\(,\\&\\')-?]{1,140}$");


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
        result.setRemittanceInformation(remittanceInformation);
        result.setEndToEndIdentification("EndToEnd");
        result.setDebtorAccount(debtorAccount);
        result.setCreditorAccount(creditorAccount);

        return result;
    }
}

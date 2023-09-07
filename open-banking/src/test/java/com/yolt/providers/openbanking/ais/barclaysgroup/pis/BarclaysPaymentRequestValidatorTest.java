package com.yolt.providers.openbanking.ais.barclaysgroup.pis;

import com.yolt.providers.openbanking.ais.barclaysgroup.common.service.pis.BarclaysUkDomesticPaymentRequestValidator;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
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
public class BarclaysPaymentRequestValidatorTest {

    private OBWriteDomestic2DataInitiation dataInitiation;
    private PaymentRequestValidator paymentRequestValidator = new BarclaysUkDomesticPaymentRequestValidator();

    private List<String> getCorrectReferences() {
        return List.of("FRESCO-037", "Í#", "&", "Ð£=\"", "/", "ÿþýüôñ", "AaA AbC");
    }

    private List<String> getIncorrectReferences() {
        return List.of("Ą", "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 is too long", ":", "{}", "€", "", " -space as first character");
    }

    @ParameterizedTest
    @MethodSource("getCorrectReferences")
    public void shouldNotThrowAnyExceptionWhenReferenceMatchPattern(String testReference) {
        //Given
        dataInitiation = getDataInitialization(testReference);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);

        //Than
        assertThatCode(callable).doesNotThrowAnyException();
    }

    @Test
    public void shouldNotThrowAnyExceptionWhenReferenceIsNull() {
        //Given
        dataInitiation = getDataInitialization(null);
        //When
        ThrowableAssert.ThrowingCallable callable = () -> paymentRequestValidator.validateRequest(dataInitiation);
        //Than
        assertThatCode(callable).doesNotThrowAnyException();
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
                .hasMessageStartingWith("Remittance information contains not allowed characters. It should match ");
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
        result.setCreditorAccount(creditorAccount);
        result.setDebtorAccount(debtorAccount);
        result.setEndToEndIdentification("EndToEnd");

        return result;
    }
}

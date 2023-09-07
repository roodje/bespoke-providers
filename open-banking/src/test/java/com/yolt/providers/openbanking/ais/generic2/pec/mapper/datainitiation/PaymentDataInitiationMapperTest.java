package com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.PaymentRequestAdjuster;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.AmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.UkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.OBWriteDomestic2DataInitiation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class PaymentDataInitiationMapperTest {

    private PaymentDataInitiationMapper subject;

    @Mock
    private Supplier<String> instructionIdentificationSupplier;

    @Mock
    private AmountFormatter amountFormatter;

    @Mock
    private UkSchemeMapper ukSchemeMapper;

    @Mock
    private PaymentRequestAdjuster<OBWriteDomestic2DataInitiation> paymentRequestAdjuster;

    @Mock
    private PaymentRequestValidator<OBWriteDomestic2DataInitiation> paymentRequestValidator;

    @Captor
    private ArgumentCaptor<OBWriteDomestic2DataInitiation> dataInitiationArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        subject = new PaymentDataInitiationMapper(instructionIdentificationSupplier, amountFormatter, ukSchemeMapper);
    }

    @Test
    void shouldReturnWriteDomesticDataInitiationWithAllSupportedFieldsWhenCorrectDataAreProvided() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMapWith(paymentRequestValidator);
        InitiateUkDomesticPaymentRequestDTO requestDTO = prepareInitiateUkDomesticPaymentRequestDTO(true, true, true, false);
        OBWriteDomestic2DataInitiation adjustedDataInitiation = new OBWriteDomestic2DataInitiation();

        given(paymentRequestAdjuster.adjust(any(OBWriteDomestic2DataInitiation.class)))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("formattedAmount");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        OBWriteDomestic2DataInitiation result = subject.map(requestDTO);

        // then
        then(paymentRequestAdjuster)
                .should()
                .adjust(dataInitiationArgumentCaptor.capture());
        then(paymentRequestValidator)
                .should()
                .validateRequest(dataInitiationArgumentCaptor.capture());
        OBWriteDomestic2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getAllValues().get(0);
        OBWriteDomestic2DataInitiation capturedAdjustedDataInitiation = dataInitiationArgumentCaptor.getAllValues().get(1);

        assertThat(result).isEqualTo(adjustedDataInitiation);
        assertThat(capturedAdjustedDataInitiation).isEqualTo(adjustedDataInitiation);
        assertThat(capturedMappedDataInitiation.getInstructionIdentification()).isEqualTo("instructionIdentification");
        assertThat(capturedMappedDataInitiation.getEndToEndIdentification()).isEqualTo("endToEndIdentification");
        assertThat(capturedMappedDataInitiation.getLocalInstrument()).isEqualTo("localInstrument");
        assertThat(capturedMappedDataInitiation.getCreditorAccount()).satisfies(creditorAccount -> {
            assertThat(creditorAccount.getName()).isEqualTo("Creditor");
            assertThat(creditorAccount.getIdentification()).isEqualTo("1234");
            assertThat(creditorAccount.getSecondaryIdentification()).isEqualTo("5678");
            assertThat(creditorAccount.getSchemeName()).isEqualTo("IBAN");
        });
        assertThat(capturedMappedDataInitiation.getDebtorAccount()).satisfies(debtorAccount -> {
            assertThat(debtorAccount.getName()).isEqualTo("Debtor");
            assertThat(debtorAccount.getIdentification()).isEqualTo("4321");
            assertThat(debtorAccount.getSecondaryIdentification()).isEqualTo("8765");
            assertThat(debtorAccount.getSchemeName()).isEqualTo("IBAN");
        });
        assertThat(capturedMappedDataInitiation.getInstructedAmount()).satisfies(instructedAmount -> {
            assertThat(instructedAmount.getAmount()).isEqualTo("formattedAmount");
            assertThat(instructedAmount.getCurrency()).isEqualTo("GBP");
        });
        assertThat(capturedMappedDataInitiation.getRemittanceInformation()).satisfies(remittanceInformation -> {
            assertThat(remittanceInformation.getReference()).isEqualTo("reference");
            assertThat(remittanceInformation.getUnstructured()).isEqualTo("unstructured");
        });
    }

    @Test
    void shouldReturnWriteDomesticDataInitiationWithoutRemittanceReferenceWhenDynamicFieldsIsNull() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMapWith(paymentRequestValidator);
        InitiateUkDomesticPaymentRequestDTO requestDTO = prepareInitiateUkDomesticPaymentRequestDTO(true, false, true, true);
        OBWriteDomestic2DataInitiation adjustedDataInitiation = new OBWriteDomestic2DataInitiation();

        given(paymentRequestAdjuster.adjust(any(OBWriteDomestic2DataInitiation.class)))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("formattedAmount");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        subject.map(requestDTO);

        // then
        then(paymentRequestAdjuster)
                .should()
                .adjust(dataInitiationArgumentCaptor.capture());
        OBWriteDomestic2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();

        assertThat(capturedMappedDataInitiation.getRemittanceInformation()).satisfies(remittanceInformation -> {
            assertThat(remittanceInformation.getReference()).isNull();
            assertThat(remittanceInformation.getUnstructured()).isEqualTo("unstructured");
        });
    }

    @Test
    void shouldReturnWriteDomesticDataInitiationWithoutRemittanceReferenceWhenDynamicFieldsDoNotContainRemittanceStructured() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMapWith(paymentRequestValidator);
        InitiateUkDomesticPaymentRequestDTO requestDTO = prepareInitiateUkDomesticPaymentRequestDTO(true, false, true, false);
        OBWriteDomestic2DataInitiation adjustedDataInitiation = new OBWriteDomestic2DataInitiation();

        given(paymentRequestAdjuster.adjust(any(OBWriteDomestic2DataInitiation.class)))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("formattedAmount");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        subject.map(requestDTO);

        // then
        then(paymentRequestAdjuster)
                .should()
                .adjust(dataInitiationArgumentCaptor.capture());
        OBWriteDomestic2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();

        assertThat(capturedMappedDataInitiation.getRemittanceInformation()).satisfies(remittanceInformation -> {
            assertThat(remittanceInformation.getReference()).isNull();
            assertThat(remittanceInformation.getUnstructured()).isEqualTo("unstructured");
        });
    }

    @Test
    void shouldReturnOBWriteDomestic2DataInitiationWithoutDebtorWhenMapDebtorFlagIsFalse() {
        // given
        subject.withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMapWith(paymentRequestValidator);
        InitiateUkDomesticPaymentRequestDTO requestDTO = prepareInitiateUkDomesticPaymentRequestDTO(true, true, true, false);
        OBWriteDomestic2DataInitiation adjustedDataInitiation = new OBWriteDomestic2DataInitiation();

        given(paymentRequestAdjuster.adjust(any(OBWriteDomestic2DataInitiation.class)))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("formattedAmount");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        subject.map(requestDTO);

        // then
        then(paymentRequestAdjuster)
                .should()
                .adjust(dataInitiationArgumentCaptor.capture());
        OBWriteDomestic2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();

        assertThat(capturedMappedDataInitiation.getDebtorAccount()).isNull();
    }

    @Test
    void shouldReturnOBWriteDomestic2DataInitiationWithoutDebtorWhenMapDebtorFlagIsTrueButDebtorAccountIsNotProvidedInRequest() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMapWith(paymentRequestValidator);
        InitiateUkDomesticPaymentRequestDTO requestDTO = prepareInitiateUkDomesticPaymentRequestDTO(false, true, true, false);
        OBWriteDomestic2DataInitiation adjustedDataInitiation = new OBWriteDomestic2DataInitiation();

        given(paymentRequestAdjuster.adjust(any(OBWriteDomestic2DataInitiation.class)))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("formattedAmount");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        subject.map(requestDTO);

        // then
        then(paymentRequestAdjuster)
                .should()
                .adjust(dataInitiationArgumentCaptor.capture());
        OBWriteDomestic2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();

        assertThat(capturedMappedDataInitiation.getDebtorAccount()).isNull();
    }

    @Test
    void shouldReturnOBWriteDomestic2DataInitiationWithRemittanceInformationReversedWhenReverseRemittanceInformationFlagIsTrue() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .reversingRemittanceInformation()
                .validateAfterMapWith(paymentRequestValidator);
        InitiateUkDomesticPaymentRequestDTO requestDTO = prepareInitiateUkDomesticPaymentRequestDTO(true, true, true, false);
        OBWriteDomestic2DataInitiation adjustedDataInitiation = new OBWriteDomestic2DataInitiation();

        given(paymentRequestAdjuster.adjust(any(OBWriteDomestic2DataInitiation.class)))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("formattedAmount");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        subject.map(requestDTO);

        // then
        then(paymentRequestAdjuster)
                .should()
                .adjust(dataInitiationArgumentCaptor.capture());
        OBWriteDomestic2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();

        assertThat(capturedMappedDataInitiation.getRemittanceInformation()).satisfies(remittanceInformation -> {
            assertThat(remittanceInformation.getReference()).isEqualTo("unstructured");
            assertThat(remittanceInformation.getUnstructured()).isEqualTo("reference");
        });
    }

    @Test
    void shouldReturnOBWriteDomestic2DataInitiationWithoutWhenRemittanceStructuredAndUnstructuredAreBothNotProvided() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .reversingRemittanceInformation()
                .validateAfterMapWith(paymentRequestValidator);
        InitiateUkDomesticPaymentRequestDTO requestDTO = prepareInitiateUkDomesticPaymentRequestDTO(true, false, false, false);
        OBWriteDomestic2DataInitiation adjustedDataInitiation = new OBWriteDomestic2DataInitiation();

        given(paymentRequestAdjuster.adjust(any(OBWriteDomestic2DataInitiation.class)))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("formattedAmount");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        subject.map(requestDTO);

        // then
        then(paymentRequestAdjuster)
                .should()
                .adjust(dataInitiationArgumentCaptor.capture());
        OBWriteDomestic2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();

        assertThat(capturedMappedDataInitiation.getRemittanceInformation()).isNull();
    }

    private InitiateUkDomesticPaymentRequestDTO prepareInitiateUkDomesticPaymentRequestDTO(boolean withDebtor,
                                                                                           boolean includeRemittanceStructured,
                                                                                           boolean includeRemittanceUnstructured,
                                                                                           boolean nullDynamicFields) {
        Map<String, String> dynamicFields = includeRemittanceStructured ? Map.of(PaymentDataInitiationMapper.REMITTANCE_INFORMATION_STRUCTURED_FIELD_NAME, "reference") : Collections.emptyMap();
        return new InitiateUkDomesticPaymentRequestDTO(
                "endToEndIdentification",
                "GBP",
                new BigDecimal("100.12"),
                new UkAccountDTO("1234", AccountIdentifierScheme.IBAN, "Creditor", "5678"),
                withDebtor ? new UkAccountDTO("4321", AccountIdentifierScheme.IBAN, "Debtor", "8765") : null,
                includeRemittanceUnstructured ? "unstructured" : null,
                nullDynamicFields ? null : dynamicFields
        );
    }
}
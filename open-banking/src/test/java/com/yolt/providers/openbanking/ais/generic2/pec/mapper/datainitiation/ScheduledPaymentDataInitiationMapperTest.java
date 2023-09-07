package com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation;

import com.yolt.providers.common.pis.ukdomestic.AccountIdentifierScheme;
import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.PaymentRequestAdjuster;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.AmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.UkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

@ExtendWith(MockitoExtension.class)
class ScheduledPaymentDataInitiationMapperTest {

    private ScheduledPaymentDataInitiationMapper subject;

    @Mock
    private Supplier<String> instructionIdentificationSupplier;

    @Mock
    private AmountFormatter amountFormatter;

    @Mock
    private UkSchemeMapper ukSchemeMapper;

    @Mock
    private PaymentRequestAdjuster<OBWriteDomesticScheduled2DataInitiation> paymentRequestAdjuster;

    @Mock
    private PaymentRequestValidator<OBWriteDomesticScheduled2DataInitiation> paymentRequestValidator;

    @Captor
    private ArgumentCaptor<OBWriteDomesticScheduled2DataInitiation> dataInitiationArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        subject = new ScheduledPaymentDataInitiationMapper(instructionIdentificationSupplier, amountFormatter, ukSchemeMapper);
    }

    @Test
    void shouldReturnWriteDomesticScheduledDataInitiationWithAllSupportedFieldsWhenCorrectDataAreProvided() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMappingWith(paymentRequestValidator);
        InitiateUkDomesticScheduledPaymentRequestDTO requestDTO = prepareInitiateUkDomesticScheduledPaymentRequestDTO(true, true, true, false, true);
        OBWriteDomesticScheduled2DataInitiation adjustedDataInitiation = new OBWriteDomesticScheduled2DataInitiation();

        given(paymentRequestAdjuster.adjust(dataInitiationArgumentCaptor.capture()))
                .willReturn(adjustedDataInitiation);
        willDoNothing().given(paymentRequestValidator).validateRequest(dataInitiationArgumentCaptor.capture());
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("1.23");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        OBWriteDomesticScheduled2DataInitiation expectedMappedDataInitiationResult = new OBWriteDomesticScheduled2DataInitiation()
                .instructionIdentification("instructionIdentification")
                .endToEndIdentification("endToEndIdentification")
                .localInstrument("localInstrument")
                .creditorAccount(new OBWriteDomestic2DataInitiationCreditorAccount()
                        .name("Creditor")
                        .identification("1234")
                        .secondaryIdentification("5678")
                        .schemeName("IBAN"))
                .debtorAccount(new OBWriteDomestic2DataInitiationDebtorAccount()
                        .name("Debtor")
                        .identification("4321")
                        .secondaryIdentification("8765")
                        .schemeName("IBAN"))
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount("1.23")
                        .currency("GBP"))
                .remittanceInformation(new OBWriteDomestic2DataInitiationRemittanceInformation()
                        .reference("reference")
                        .unstructured("unstructured"));

        // when
        OBWriteDomesticScheduled2DataInitiation result = subject.map(requestDTO);

        // then
        OBWriteDomesticScheduled2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getAllValues().get(0);
        OBWriteDomesticScheduled2DataInitiation capturedAdjustedDataInitiation = dataInitiationArgumentCaptor.getAllValues().get(1);

        assertThat(result).isEqualTo(adjustedDataInitiation);
        assertThat(capturedAdjustedDataInitiation).isEqualTo(adjustedDataInitiation);
        assertThat(capturedMappedDataInitiation).isEqualTo(expectedMappedDataInitiationResult);
    }

    @Test
    void shouldReturnWriteDomesticScheduledDataInitiationWithoutRemittanceReferenceWhenDynamicFieldsIsNull() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMappingWith(paymentRequestValidator);
        InitiateUkDomesticScheduledPaymentRequestDTO requestDTO = prepareInitiateUkDomesticScheduledPaymentRequestDTO(true, false, true, true, true);
        OBWriteDomesticScheduled2DataInitiation adjustedDataInitiation = new OBWriteDomesticScheduled2DataInitiation();

        given(paymentRequestAdjuster.adjust(dataInitiationArgumentCaptor.capture()))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("1.23");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        OBWriteDomestic2DataInitiationRemittanceInformation expectedRemittanceInformation = new OBWriteDomestic2DataInitiationRemittanceInformation()
                .unstructured("unstructured");

        // when
        subject.map(requestDTO);

        // then
        OBWriteDomesticScheduled2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();
        assertThat(capturedMappedDataInitiation.getRemittanceInformation()).isEqualTo(expectedRemittanceInformation);
    }

    @Test
    void shouldReturnWriteDomesticScheduledDataInitiationWithoutRemittanceReferenceWhenDynamicFieldsDoNotContainRemittanceStructured() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMappingWith(paymentRequestValidator);
        InitiateUkDomesticScheduledPaymentRequestDTO requestDTO = prepareInitiateUkDomesticScheduledPaymentRequestDTO(true, false, true, false, true);
        OBWriteDomesticScheduled2DataInitiation adjustedDataInitiation = new OBWriteDomesticScheduled2DataInitiation();

        given(paymentRequestAdjuster.adjust(dataInitiationArgumentCaptor.capture()))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("1.23");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        OBWriteDomestic2DataInitiationRemittanceInformation expectedRemittanceInformation = new OBWriteDomestic2DataInitiationRemittanceInformation()
                .unstructured("unstructured");

        // when
        subject.map(requestDTO);

        // then
        OBWriteDomesticScheduled2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();
        assertThat(capturedMappedDataInitiation.getRemittanceInformation()).isEqualTo(expectedRemittanceInformation);
    }

    @Test
    void shouldReturnOBWriteDomesticScheduled2DataInitiationWithoutDebtorWhenMapDebtorFlagIsFalse() {
        // given
        subject.withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMappingWith(paymentRequestValidator);
        InitiateUkDomesticScheduledPaymentRequestDTO requestDTO = prepareInitiateUkDomesticScheduledPaymentRequestDTO(true, true, true, false, true);
        OBWriteDomesticScheduled2DataInitiation adjustedDataInitiation = new OBWriteDomesticScheduled2DataInitiation();

        given(paymentRequestAdjuster.adjust(dataInitiationArgumentCaptor.capture()))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("1.23");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        subject.map(requestDTO);

        // then
        OBWriteDomesticScheduled2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();
        assertThat(capturedMappedDataInitiation.getDebtorAccount()).isNull();
    }

    @Test
    void shouldReturnOBWriteDomesticScheduled2DataInitiationWithoutDebtorWhenMapDebtorFlagIsTrueButDebtorAccountIsNotProvidedInRequest() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMappingWith(paymentRequestValidator);
        InitiateUkDomesticScheduledPaymentRequestDTO requestDTO = prepareInitiateUkDomesticScheduledPaymentRequestDTO(false, true, true, false, true);
        OBWriteDomesticScheduled2DataInitiation adjustedDataInitiation = new OBWriteDomesticScheduled2DataInitiation();

        given(paymentRequestAdjuster.adjust(dataInitiationArgumentCaptor.capture()))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("1.23");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        subject.map(requestDTO);

        // then
        OBWriteDomesticScheduled2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();
        assertThat(capturedMappedDataInitiation.getDebtorAccount()).isNull();
    }

    @Test
    void shouldReturnOBWriteDomesticScheduled2DataInitiationWithoutWhenRemittanceStructuredAndUnstructuredAreBothNotProvided() {
        // given
        subject.withDebtorAccount()
                .withLocalInstrument("localInstrument")
                .withAdjuster(paymentRequestAdjuster)
                .validateAfterMappingWith(paymentRequestValidator);
        InitiateUkDomesticScheduledPaymentRequestDTO requestDTO = prepareInitiateUkDomesticScheduledPaymentRequestDTO(true, false, false, false, true);
        OBWriteDomesticScheduled2DataInitiation adjustedDataInitiation = new OBWriteDomesticScheduled2DataInitiation();

        given(paymentRequestAdjuster.adjust(dataInitiationArgumentCaptor.capture()))
                .willReturn(adjustedDataInitiation);
        given(instructionIdentificationSupplier.get())
                .willReturn("instructionIdentification");
        given(amountFormatter.format(any(BigDecimal.class)))
                .willReturn("1.23");
        given(ukSchemeMapper.map(any(AccountIdentifierScheme.class)))
                .willReturn("IBAN");

        // when
        subject.map(requestDTO);

        // then
        OBWriteDomesticScheduled2DataInitiation capturedMappedDataInitiation = dataInitiationArgumentCaptor.getValue();
        assertThat(capturedMappedDataInitiation.getRemittanceInformation()).isNull();
    }

    private InitiateUkDomesticScheduledPaymentRequestDTO prepareInitiateUkDomesticScheduledPaymentRequestDTO(boolean withDebtor,
                                                                                                             boolean includeRemittanceStructured,
                                                                                                             boolean includeRemittanceUnstructured,
                                                                                                             boolean nullDynamicFields,
                                                                                                             boolean nullExecutionDate) {
        Map<String, String> dynamicFields = includeRemittanceStructured ? Map.of(PaymentDataInitiationMapper.REMITTANCE_INFORMATION_STRUCTURED_FIELD_NAME, "reference") : Collections.emptyMap();
        OffsetDateTime executionDate = OffsetDateTime.now(Clock.systemDefaultZone());
        return new InitiateUkDomesticScheduledPaymentRequestDTO(
                "endToEndIdentification",
                "GBP",
                new BigDecimal("100.12"),
                new UkAccountDTO("1234", AccountIdentifierScheme.IBAN, "Creditor", "5678"),
                withDebtor ? new UkAccountDTO("4321", AccountIdentifierScheme.IBAN, "Debtor", "8765") : null,
                includeRemittanceUnstructured ? "unstructured" : null,
                nullDynamicFields ? null : dynamicFields,
                nullExecutionDate ? null : executionDate
        );
    }
}
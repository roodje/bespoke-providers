package com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticScheduledPaymentRequestDTO;
import com.yolt.providers.common.pis.ukdomestic.UkAccountDTO;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.DataMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.PaymentRequestAdjuster;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.amount.AmountFormatter;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.scheme.UkSchemeMapper;
import com.yolt.providers.openbanking.ais.generic2.pec.mapper.validator.PaymentRequestValidator;
import com.yolt.providers.openbanking.dto.pis.openbanking316.*;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class ScheduledPaymentDataInitiationMapper implements DataMapper<OBWriteDomesticScheduled2DataInitiation, InitiateUkDomesticScheduledPaymentRequestDTO> {

    public static final String REMITTANCE_INFORMATION_STRUCTURED_DYN_FIELD_NAME = "remittanceInformationStructured";

    private boolean mapDebtor;
    private String localInstrument;

    private final Supplier<String> instructionIdentificationSupplier;
    private final AmountFormatter amountFormatter;
    private final UkSchemeMapper ukSchemeMapper;

    private PaymentRequestValidator<OBWriteDomesticScheduled2DataInitiation> paymentRequestValidator = dataInitiation -> {
    };
    private PaymentRequestAdjuster<OBWriteDomesticScheduled2DataInitiation> paymentRequestAdjuster = dataInitiation -> dataInitiation;

    public ScheduledPaymentDataInitiationMapper withAdjuster(PaymentRequestAdjuster<OBWriteDomesticScheduled2DataInitiation> adjuster) {
        this.paymentRequestAdjuster = adjuster;
        return this;
    }

    public ScheduledPaymentDataInitiationMapper withDebtorAccount() {
        this.mapDebtor = true;
        return this;
    }

    public ScheduledPaymentDataInitiationMapper withLocalInstrument(String localInstrument) {
        this.localInstrument = localInstrument;
        return this;
    }

    public ScheduledPaymentDataInitiationMapper validateAfterMappingWith(PaymentRequestValidator<OBWriteDomesticScheduled2DataInitiation> paymentRequestValidator) {
        this.paymentRequestValidator = paymentRequestValidator;
        return this;
    }

    @Override
    public OBWriteDomesticScheduled2DataInitiation map(InitiateUkDomesticScheduledPaymentRequestDTO requestDTO) {
        var remittanceUnstructured = requestDTO.getRemittanceInformationUnstructured();
        var remittanceStructured = requestDTO.getDynamicFields() != null ? requestDTO.getDynamicFields().get(REMITTANCE_INFORMATION_STRUCTURED_DYN_FIELD_NAME) : null;
        var dataInitiation = new OBWriteDomesticScheduled2DataInitiation()
                .creditorAccount(mapCreditorAccount(requestDTO.getCreditorAccount()))
                .debtorAccount(mapDebtor ? mapDebtorAccount(requestDTO.getDebtorAccount()) : null)
                .instructionIdentification(instructionIdentificationSupplier.get())
                .endToEndIdentification(requestDTO.getEndToEndIdentification())
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount(amountFormatter.format(requestDTO.getAmount()))
                        .currency(requestDTO.getCurrencyCode()))
                .localInstrument(localInstrument)
                .remittanceInformation(mapRemittanceInformation(remittanceUnstructured, remittanceStructured))
                .requestedExecutionDateTime(requestDTO.getExecutionDate());
        dataInitiation = paymentRequestAdjuster.adjust(dataInitiation);
        paymentRequestValidator.validateRequest(dataInitiation);
        return dataInitiation;
    }

    private OBWriteDomestic2DataInitiationRemittanceInformation mapRemittanceInformation(String remittanceUnstructured,
                                                                                         String remittanceStructured) {
        if (StringUtils.isEmpty(remittanceStructured) && StringUtils.isEmpty(remittanceUnstructured)) {
            return null;
        }
        return new OBWriteDomestic2DataInitiationRemittanceInformation()
                .reference(remittanceStructured)
                .unstructured(remittanceUnstructured);
    }

    private OBWriteDomestic2DataInitiationDebtorAccount mapDebtorAccount(UkAccountDTO debtorAccount) {
        return debtorAccount == null ? null : new OBWriteDomestic2DataInitiationDebtorAccount()
                .name(debtorAccount.getAccountHolderName())
                .identification(debtorAccount.getAccountIdentifier())
                .secondaryIdentification(debtorAccount.getSecondaryIdentification())
                .schemeName(ukSchemeMapper.map(debtorAccount.getAccountIdentifierScheme()));
    }

    private OBWriteDomestic2DataInitiationCreditorAccount mapCreditorAccount(UkAccountDTO creditorAccount) {
        return new OBWriteDomestic2DataInitiationCreditorAccount()
                .name(creditorAccount.getAccountHolderName())
                .identification(creditorAccount.getAccountIdentifier())
                .secondaryIdentification(creditorAccount.getSecondaryIdentification())
                .schemeName(ukSchemeMapper.map(creditorAccount.getAccountIdentifierScheme()));
    }
}

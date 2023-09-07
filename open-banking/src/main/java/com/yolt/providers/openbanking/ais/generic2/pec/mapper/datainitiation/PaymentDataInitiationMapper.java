package com.yolt.providers.openbanking.ais.generic2.pec.mapper.datainitiation;

import com.yolt.providers.common.pis.ukdomestic.InitiateUkDomesticPaymentRequestDTO;
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
public class PaymentDataInitiationMapper implements DataMapper<OBWriteDomestic2DataInitiation, InitiateUkDomesticPaymentRequestDTO> {

    public static final String REMITTANCE_INFORMATION_STRUCTURED_FIELD_NAME = "remittanceInformationStructured";

    private boolean mapDebtor;
    private boolean reverseRemittanceInformation;
    private String localInstrument;

    private final Supplier<String> instructionIdentificationSupplier;
    private final AmountFormatter amountFormatter;
    private final UkSchemeMapper ukSchemeMapper;

    private PaymentRequestValidator<OBWriteDomestic2DataInitiation> paymentRequestValidator = dataInitiation -> {
    };
    private PaymentRequestAdjuster<OBWriteDomestic2DataInitiation> paymentRequestAdjuster = dataInitiation -> dataInitiation;

    public PaymentDataInitiationMapper withAdjuster(PaymentRequestAdjuster<OBWriteDomestic2DataInitiation> adjuster) {
        this.paymentRequestAdjuster = adjuster;
        return this;
    }

    public PaymentDataInitiationMapper withDebtorAccount() {
        this.mapDebtor = true;
        return this;
    }

    public PaymentDataInitiationMapper withLocalInstrument(String localInstrument) {
        this.localInstrument = localInstrument;
        return this;
    }

    public PaymentDataInitiationMapper validateAfterMapWith(PaymentRequestValidator<OBWriteDomestic2DataInitiation> paymentRequestValidator) {
        this.paymentRequestValidator = paymentRequestValidator;
        return this;
    }

    public PaymentDataInitiationMapper reversingRemittanceInformation() {
        this.reverseRemittanceInformation = true;
        return this;
    }

    @Override
    public OBWriteDomestic2DataInitiation map(InitiateUkDomesticPaymentRequestDTO requestDTO) {
        var remittanceUnstructured = requestDTO.getRemittanceInformationUnstructured();
        var remittanceStructured = requestDTO.getDynamicFields() != null ? requestDTO.getDynamicFields().get(REMITTANCE_INFORMATION_STRUCTURED_FIELD_NAME) : null;
        var dataInitiation = new OBWriteDomestic2DataInitiation()
                .creditorAccount(mapCreditorAccount(requestDTO.getCreditorAccount()))
                .debtorAccount(mapDebtor ? mapDebtorAccount(requestDTO.getDebtorAccount()) : null)
                .instructionIdentification(instructionIdentificationSupplier.get())
                .endToEndIdentification(requestDTO.getEndToEndIdentification())
                .instructedAmount(new OBWriteDomestic2DataInitiationInstructedAmount()
                        .amount(amountFormatter.format(requestDTO.getAmount()))
                        .currency(requestDTO.getCurrencyCode()))
                .localInstrument(localInstrument)
                .remittanceInformation(mapRemittanceInformation(remittanceUnstructured, remittanceStructured));
        dataInitiation = paymentRequestAdjuster.adjust(dataInitiation);
        paymentRequestValidator.validateRequest(dataInitiation);
        return dataInitiation;
    }

    private OBWriteDomestic2DataInitiationRemittanceInformation mapRemittanceInformation(String remittanceUnstructured,
                                                                                         String remittanceStructured) {
        if (StringUtils.isEmpty(remittanceStructured) && StringUtils.isEmpty(remittanceUnstructured)) {
            return null;
        }

        var reference = reverseRemittanceInformation ? remittanceUnstructured : remittanceStructured;
        var unstructured = reverseRemittanceInformation ? remittanceStructured : remittanceUnstructured;
        return new OBWriteDomestic2DataInitiationRemittanceInformation()
                .reference(reference)
                .unstructured(unstructured);
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

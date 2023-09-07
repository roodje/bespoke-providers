package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.List;

@Getter
public class StetCreditTransferTransactionNoUnstructured extends StetCreditTransferTransaction {

    @Builder
    public StetCreditTransferTransactionNoUnstructured(StetPaymentIdentificationDTO paymentId, String resourceId, OffsetDateTime requestedExecutionDate, StetAmountTypeDTO instructedAmount, StetPartyIdentificationDTO ultimateDebtor, StetPaymentBeneficiaryDTO beneficiary, StetPartyIdentificationDTO ultimateCreditor, List<String> remittanceInformation) {
        super(paymentId, resourceId, requestedExecutionDate, instructedAmount, ultimateDebtor, beneficiary, ultimateCreditor);
        this.remittanceInformation = remittanceInformation;
    }

    private List<String> remittanceInformation;
}

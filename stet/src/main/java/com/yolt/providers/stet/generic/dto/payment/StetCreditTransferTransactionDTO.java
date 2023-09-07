package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
public class StetCreditTransferTransactionDTO extends StetCreditTransferTransaction {

  @Builder
  public StetCreditTransferTransactionDTO(StetPaymentIdentificationDTO paymentId,
                                          String resourceId,
                                          OffsetDateTime requestedExecutionDate,
                                          StetAmountTypeDTO instructedAmount,
                                          StetPartyIdentificationDTO ultimateDebtor,
                                          StetPaymentBeneficiaryDTO beneficiary,
                                          StetPartyIdentificationDTO ultimateCreditor,
                                          StetRemittanceInformationDTO remittanceInformation)  {
    super(paymentId, resourceId, requestedExecutionDate, instructedAmount, ultimateDebtor, beneficiary, ultimateCreditor);
    this.remittanceInformation = remittanceInformation;
  }

  private StetRemittanceInformationDTO remittanceInformation;
}
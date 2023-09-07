package com.yolt.providers.stet.generic.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@AllArgsConstructor
public abstract class StetCreditTransferTransaction {

  protected StetPaymentIdentificationDTO paymentId;
  protected String resourceId;
  protected OffsetDateTime requestedExecutionDate;
  protected StetAmountTypeDTO instructedAmount;
  protected StetPartyIdentificationDTO ultimateDebtor;
  protected StetPaymentBeneficiaryDTO beneficiary;
  protected StetPartyIdentificationDTO ultimateCreditor;
}
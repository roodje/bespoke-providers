package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class StetPaymentBeneficiaryDTO {

  private StetPartyIdentificationDTO creditor;
  private StetAccountIdentificationDTO creditorAccount;
  private StetFinancialInstitutionIdentificationDTO creditorAgent;
}
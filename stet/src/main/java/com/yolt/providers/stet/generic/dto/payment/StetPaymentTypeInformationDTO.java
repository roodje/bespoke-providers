package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class StetPaymentTypeInformationDTO {

  private StetPriorityCode instructionPriority;
  private StetServiceLevel serviceLevel;
  private String localInstrument;
  private StetCategoryPurpose categoryPurpose;
}
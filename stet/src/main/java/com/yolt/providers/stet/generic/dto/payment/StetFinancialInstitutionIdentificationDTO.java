package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StetFinancialInstitutionIdentificationDTO {

  private String bicFi;
  private String name;
}
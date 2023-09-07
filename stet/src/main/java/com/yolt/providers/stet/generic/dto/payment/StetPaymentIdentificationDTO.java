package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StetPaymentIdentificationDTO {

  private String instructionId;
  private String endToEndId;
}
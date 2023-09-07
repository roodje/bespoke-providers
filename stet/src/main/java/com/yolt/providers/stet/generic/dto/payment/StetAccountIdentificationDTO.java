package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StetAccountIdentificationDTO {

  private String iban;
  private String currency;
}
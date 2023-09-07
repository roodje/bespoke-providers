package com.yolt.providers.stet.generic.dto.payment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StetAmountTypeDTO {

  private Float amount;
  private String currency;
}
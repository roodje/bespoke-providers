package com.yolt.providers.stet.generic.dto.balance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StetBalanceType {

  CLBD("CLBD"),
  XPCD("XPCD"),
  VALU("VALU"),
  OTHR("OTHR"),
  ITAV("ITAV");

  private final String value;
}
package com.yolt.providers.stet.generic.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StetAuthenticationApproach {

  REDIRECT("REDIRECT"),
  DECOUPLED("DECOUPLED"),
  EMBEDDED_1_FACTOR("EMBEDDED-1-FACTOR"),
  NONE("NONE");

  private final String value;
}
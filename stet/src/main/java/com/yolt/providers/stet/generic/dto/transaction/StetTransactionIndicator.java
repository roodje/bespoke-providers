package com.yolt.providers.stet.generic.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Known in STET as CreditDebitIndicator
 */
@Getter
@AllArgsConstructor
public enum StetTransactionIndicator {
  
  CRDT("CRDT"),
  DBIT("DBIT");

  private final String value;
}
package com.yolt.providers.redsys.common.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * RequestGetConsent
 */
@Data
@Builder
public final class RequestGetConsent {
  @NotNull
  @Valid
  private final AccountAccess access;

  private final Boolean recurringIndicator;

  private final String validUntil;

  private final Integer frequencyPerDay;

  private final Boolean combinedServiceIndicator;

}

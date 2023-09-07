package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * Message codes for HTTP codes 200 to a Payment Initiation Status Request.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum MessageCode200InitiationStatus {
  
  FUNDS_NOT_AVAILABLE("FUNDS_NOT_AVAILABLE");

  private String value;

  MessageCode200InitiationStatus(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode200InitiationStatus fromValue(String value) {
    for (MessageCode200InitiationStatus b : MessageCode200InitiationStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


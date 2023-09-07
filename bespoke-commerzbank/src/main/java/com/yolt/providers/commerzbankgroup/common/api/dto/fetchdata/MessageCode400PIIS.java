package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * Message codes defined for PIIS for HTTP Error code 400 (BAD_REQUEST).
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum MessageCode400PIIS {
  
  FORMAT_ERROR("FORMAT_ERROR"),
  
  PARAMETER_NOT_CONSISTENT("PARAMETER_NOT_CONSISTENT"),
  
  PARAMETER_NOT_SUPPORTED("PARAMETER_NOT_SUPPORTED"),
  
  SERVICE_INVALID("SERVICE_INVALID"),
  
  RESOURCE_UNKNOWN("RESOURCE_UNKNOWN"),
  
  RESOURCE_EXPIRED("RESOURCE_EXPIRED"),
  
  RESOURCE_BLOCKED("RESOURCE_BLOCKED"),
  
  TIMESTAMP_INVALID("TIMESTAMP_INVALID"),
  
  PERIOD_INVALID("PERIOD_INVALID"),
  
  SCA_METHOD_UNKNOWN("SCA_METHOD_UNKNOWN"),
  
  SCA_INVALID("SCA_INVALID"),
  
  CONSENT_UNKNOWN("CONSENT_UNKNOWN"),
  
  CARD_INVALID("CARD_INVALID"),
  
  NO_PIIS_ACTIVATION("NO_PIIS_ACTIVATION");

  private String value;

  MessageCode400PIIS(String value) {
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
  public static MessageCode400PIIS fromValue(String value) {
    for (MessageCode400PIIS b : MessageCode400PIIS.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


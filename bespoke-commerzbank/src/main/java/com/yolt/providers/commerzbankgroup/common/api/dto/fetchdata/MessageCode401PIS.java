package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * Message codes defined for PIS for HTTP Error code 401 (UNAUTHORIZED).
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum MessageCode401PIS {
  
  CERTIFICATE_INVALID("CERTIFICATE_INVALID"),
  
  ROLE_INVALID("ROLE_INVALID"),
  
  CERTIFICATE_EXPIRED("CERTIFICATE_EXPIRED"),
  
  CERTIFICATE_BLOCKED("CERTIFICATE_BLOCKED"),
  
  CERTIFICATE_REVOKE("CERTIFICATE_REVOKE"),
  
  CERTIFICATE_MISSING("CERTIFICATE_MISSING"),
  
  SIGNATURE_INVALID("SIGNATURE_INVALID"),
  
  SIGNATURE_MISSING("SIGNATURE_MISSING"),
  
  CORPORATE_ID_INVALID("CORPORATE_ID_INVALID"),
  
  PSU_CREDENTIALS_INVALID("PSU_CREDENTIALS_INVALID"),
  
  CONSENT_INVALID("CONSENT_INVALID"),
  
  CONSENT_EXPIRED("CONSENT_EXPIRED"),
  
  TOKEN_UNKNOWN("TOKEN_UNKNOWN"),
  
  TOKEN_INVALID("TOKEN_INVALID"),
  
  TOKEN_EXPIRED("TOKEN_EXPIRED"),
  
  KID_MISSING("KID_MISSING");

  private String value;

  MessageCode401PIS(String value) {
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
  public static MessageCode401PIS fromValue(String value) {
    for (MessageCode401PIS b : MessageCode401PIS.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


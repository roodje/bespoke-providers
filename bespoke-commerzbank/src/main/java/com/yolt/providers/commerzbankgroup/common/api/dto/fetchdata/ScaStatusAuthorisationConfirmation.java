package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * This data element is containing information about the status of the SCA method in an authorisation confirmation response.   The following codes are defined for this data type.    * 'finalised': if the transaction authorisation and confirmation was successfule.   * 'failed': if the transaction authorisation or confirmation was not successful. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum ScaStatusAuthorisationConfirmation {
  
  FINALISED("finalised"),
  
  FAILED("failed");

  private String value;

  ScaStatusAuthorisationConfirmation(String value) {
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
  public static ScaStatusAuthorisationConfirmation fromValue(String value) {
    for (ScaStatusAuthorisationConfirmation b : ScaStatusAuthorisationConfirmation.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


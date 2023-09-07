package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * ExternalServiceLevel1Code from ISO 20022.  Values from ISO 20022 External Code List ExternalCodeSets_1Q2021 May 2021. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum ServiceLevelCode {
  
  BKTR("BKTR"),
  
  G001("G001"),
  
  G002("G002"),
  
  G003("G003"),
  
  G004("G004"),
  
  NPCA("NPCA"),
  
  NUGP("NUGP"),
  
  NURG("NURG"),
  
  PRPT("PRPT"),
  
  SDVA("SDVA"),
  
  SEPA("SEPA"),
  
  SVDE("SVDE"),
  
  URGP("URGP"),
  
  URNS("URNS");

  private String value;

  ServiceLevelCode(String value) {
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
  public static ServiceLevelCode fromValue(String value) {
    for (ServiceLevelCode b : ServiceLevelCode.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


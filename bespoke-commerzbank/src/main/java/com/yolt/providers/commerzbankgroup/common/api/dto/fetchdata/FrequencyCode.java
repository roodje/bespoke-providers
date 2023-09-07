package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * The following codes from the \"EventFrequency7Code\" of ISO 20022 are supported: - \"Daily\" - \"Weekly\" - \"EveryTwoWeeks\" - \"Monthly\" - \"EveryTwoMonths\" - \"Quarterly\" - \"SemiAnnual\" - \"Annual\" - \"MonthlyVariable\" 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum FrequencyCode {
  
  DAILY("Daily"),
  
  WEEKLY("Weekly"),
  
  EVERYTWOWEEKS("EveryTwoWeeks"),
  
  MONTHLY("Monthly"),
  
  EVERYTWOMONTHS("EveryTwoMonths"),
  
  QUARTERLY("Quarterly"),
  
  SEMIANNUAL("SemiAnnual"),
  
  ANNUAL("Annual"),
  
  MONTHLYVARIABLE("MonthlyVariable");

  private String value;

  FrequencyCode(String value) {
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
  public static FrequencyCode fromValue(String value) {
    for (FrequencyCode b : FrequencyCode.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * \"following\" or \"preceding\" supported as values.  This data attribute defines the behaviour when recurring payment dates falls on a weekend or bank holiday.  The payment is then executed either the \"preceding\" or \"following\" working day. ASPSP might reject the request due to the communicated value, if rules in Online-Banking are not supporting  this execution rule. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum ExecutionRule {
  
  FOLLOWING("following"),
  
  PRECEDING("preceding");

  private String value;

  ExecutionRule(String value) {
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
  public static ExecutionRule fromValue(String value) {
    for (ExecutionRule b : ExecutionRule.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


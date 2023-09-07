package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * Day of execution as string.  This string consists of up two characters. Leading zeroes are not allowed.  31 is ultimo of the month. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum DayOfExecution {
  
  _1("1"),
  
  _2("2"),
  
  _3("3"),
  
  _4("4"),
  
  _5("5"),
  
  _6("6"),
  
  _7("7"),
  
  _8("8"),
  
  _9("9"),
  
  _10("10"),
  
  _11("11"),
  
  _12("12"),
  
  _13("13"),
  
  _14("14"),
  
  _15("15"),
  
  _16("16"),
  
  _17("17"),
  
  _18("18"),
  
  _19("19"),
  
  _20("20"),
  
  _21("21"),
  
  _22("22"),
  
  _23("23"),
  
  _24("24"),
  
  _25("25"),
  
  _26("26"),
  
  _27("27"),
  
  _28("28"),
  
  _29("29"),
  
  _30("30"),
  
  _31("31");

  private String value;

  DayOfExecution(String value) {
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
  public static DayOfExecution fromValue(String value) {
    for (DayOfExecution b : DayOfExecution.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


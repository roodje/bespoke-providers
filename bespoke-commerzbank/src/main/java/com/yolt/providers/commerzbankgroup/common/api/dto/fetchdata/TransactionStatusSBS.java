package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * The transaction status is filled with codes of the ISO 20022 data table. Only the codes RCVD, PATC, ACTC, ACWC and RJCT are used: - 'ACSP': 'AcceptedSettlementInProcess' -    All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution. - 'ACTC': 'AcceptedTechnicalValidation' -    Authentication and syntactical and semantical validation are successful. - 'ACWC': 'AcceptedWithChange' -    Instruction is accepted but a change will be made, such as date or remittance not sent. - 'RCVD': 'Received' -    Payment initiation has been received by the receiving agent. - 'RJCT': 'Rejected' -    Payment initiation or individual transaction included in the payment initiation has been rejected. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum TransactionStatusSBS {
  
  ACSC("ACSC"),
  
  ACTC("ACTC"),
  
  PATC("PATC"),
  
  RCVD("RCVD"),
  
  RJCT("RJCT"),
  
  CANC("CANC");

  private String value;

  TransactionStatusSBS(String value) {
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
  public static TransactionStatusSBS fromValue(String value) {
    for (TransactionStatusSBS b : TransactionStatusSBS.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


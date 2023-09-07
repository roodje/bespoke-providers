package com.yolt.providers.commerzbankgroup.common.api.dto.fetchdata;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import javax.annotation.Generated;

/**
 * This data element is containing information about the status of the SCA method applied.   The following codes are defined for this data type.    * 'received':     An authorisation or cancellation-authorisation resource has been created successfully.   * 'psuIdentified':     The PSU related to the authorisation or cancellation-authorisation resource has been identified.   * 'psuAuthenticated':     The PSU related to the authorisation or cancellation-authorisation resource has been identified and authenticated e.g. by a password or by an access token.   * 'scaMethodSelected':     The PSU/TPP has selected the related SCA routine.      If the SCA method is chosen implicitly since only one SCA method is available,      then this is the first status to be reported instead of 'received'.   * 'unconfirmed':     SCA is technically successfully finalised by the PSU, but the authorisation resource needs a confirmation command by the TPP yet.    * 'started':     The addressed SCA routine has been started.   * 'finalised':     The SCA routine has been finalised successfully (including a potential confirmation command).      This is a final status of the authorisation resource.   * 'failed':     The SCA routine failed.     This is a final status of the authorisation resource.   * 'exempted':     SCA was exempted for the related transaction, the related authorisation is successful.     This is a final status of the authorisation resource. 
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-06-10T11:00:09.615090+02:00[Europe/Warsaw]")
public enum ScaStatus {
  
  RECEIVED("received"),
  
  PSUIDENTIFIED("psuIdentified"),
  
  PSUAUTHENTICATED("psuAuthenticated"),
  
  SCAMETHODSELECTED("scaMethodSelected"),
  
  STARTED("started"),
  
  UNCONFIRMED("unconfirmed"),
  
  FINALISED("finalised"),
  
  FAILED("failed"),
  
  EXEMPTED("exempted");

  private String value;

  ScaStatus(String value) {
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
  public static ScaStatus fromValue(String value) {
    for (ScaStatus b : ScaStatus.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}


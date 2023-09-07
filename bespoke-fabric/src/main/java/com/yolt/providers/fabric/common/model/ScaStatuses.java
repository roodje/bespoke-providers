package com.yolt.providers.fabric.common.model;

import lombok.Getter;

@Getter
public enum ScaStatuses {

    /**  An authorisation or cancellation-authorisation resource has been created successfully.  **/
    RECEIVED("received"),

    /**  The PSU related to the authorisation or cancellation-authorisation resource has been identified.  **/
    PSU_IDENTIFIED("psuIdentified"),

    /**  The PSU related to the authorisation or cancellation-authorisation resource has been identified and authenticated e.g. by a password or by an access token.  **/
    PSU_AUTHENTICATED("psuAuthenticated"),

    /**  The PSU/TPP has selected the related SCA routine. If the SCA method is chosen implicitly since only one SCA method is available, then this is the first status to be reported instead of 'received'.  **/
    SCA_METHOD_SELECTED("scaMethodSelected"),

    /**  The addressed SCA routine has been started.  **/
    STARTED("started"),

    /**  The SCA routine has been finalised successfully.  **/
    FINALISED("finalised"),

    /**  The SCA routine failed.  **/
    FAILED("failed"),

    /**  SCA was exempted for the related transaction, the related authorisation is successful.  **/
    EXEMPTED("exempted");

    private String value;
    ScaStatuses(String value) {
        this.value = value;
    }
}

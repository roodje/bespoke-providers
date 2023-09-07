package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Content of the body of a Transaction Authorisation Request.
 */
@Data
public class TransactionAuthorisation {

    @JsonProperty("scaAuthenticationData")
    private String scaAuthenticationData;

    public TransactionAuthorisation scaAuthenticationData(String scaAuthenticationData) {
        this.scaAuthenticationData = scaAuthenticationData;
        return this;
    }
}


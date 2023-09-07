package com.yolt.providers.triodosbank.common.model.http;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yolt.providers.triodosbank.common.model.Access;
import com.yolt.providers.triodosbank.common.model.Links;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentCreationResponse {

    private Access access;
    private Boolean recurringIndicator;
    private String validUntil;
    private Integer frequencyPerDay;
    private String lastActionDate;
    private String consentStatus;
    private String consentId;
    private String authorisationId;

    @JsonProperty("_links")
    private Links links;
}

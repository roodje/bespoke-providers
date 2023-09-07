package com.yolt.providers.commerzbankgroup.common.api.dto.authorization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsentCreationResponse {

    private AccountAccessEnum access;
    private Boolean recurringIndicator;
    private String validUntil;
    private Integer frequencyPerDay;
    private String lastActionDate;
    private String consentStatus;
    private String consentId;
    private String authorisationId;

    @JsonProperty("_links")
    private Links links;

    @Data
    public static class Links {

        private ScaOAuth scaOAuth;
    }

    @Data
    public static class ScaOAuth {
        private String href;
    }
}
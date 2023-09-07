package com.yolt.providers.redsys.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseGetConsent {
    private String consentStatus;

    private String psuMessage;

    private TppMessage tppMessage;

    private String consentId;

    private List<AuthenticationObject> scaMethods;

    @JsonProperty("_links")
    private Links links;
}

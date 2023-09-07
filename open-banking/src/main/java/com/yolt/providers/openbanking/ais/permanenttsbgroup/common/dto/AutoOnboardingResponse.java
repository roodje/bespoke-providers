package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AutoOnboardingResponse {

    @JsonProperty("Client_ID")
    private String clientId;

    @JsonProperty("Client_Secret")
    private String clientSecret;
}
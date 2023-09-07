package com.yolt.providers.openbanking.ais.revolutgroup.common.service.autoonboarding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RevolutAutoOnboardingResponse {

    @JsonProperty("client_id")
    private String clientId;
}

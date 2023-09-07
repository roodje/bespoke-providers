package com.yolt.providers.gruppocedacri.common.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AutoOnboardingResponse {

    String uuid;

    @JsonProperty("AISP")
    ApplicationConfiguration aisp;

    @JsonProperty("PISP")
    ApplicationConfiguration pisp;
}

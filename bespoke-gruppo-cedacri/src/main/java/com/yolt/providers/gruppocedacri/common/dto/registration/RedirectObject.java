package com.yolt.providers.gruppocedacri.common.dto.registration;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RedirectObject {

    @JsonProperty("AISP")
    List<String> aisp;

    @JsonProperty("PISP")
    List<String> pisp;

    @JsonProperty("CISP")
    List<String> cisp;
}

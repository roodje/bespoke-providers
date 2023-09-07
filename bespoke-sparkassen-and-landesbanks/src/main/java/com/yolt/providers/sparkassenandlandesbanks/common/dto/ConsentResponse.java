package com.yolt.providers.sparkassenandlandesbanks.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ConsentResponse {
    private String consentId;

    @JsonProperty("_links")
    private Links links;
}

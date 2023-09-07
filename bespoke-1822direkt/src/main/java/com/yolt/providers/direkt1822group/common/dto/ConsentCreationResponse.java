package com.yolt.providers.direkt1822group.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsentCreationResponse {

    private String consentId;

    @JsonProperty("_links")
    private Links links;
}

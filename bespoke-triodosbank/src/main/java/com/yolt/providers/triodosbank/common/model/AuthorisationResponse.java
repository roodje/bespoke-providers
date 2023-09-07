package com.yolt.providers.triodosbank.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AuthorisationResponse {

    private String scaStatus;
    private String authorisationId;

    @JsonProperty("_links")
    private Links authorisationLinks;
}

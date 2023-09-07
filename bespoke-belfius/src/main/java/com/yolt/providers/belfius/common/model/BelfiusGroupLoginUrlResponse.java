package com.yolt.providers.belfius.common.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class BelfiusGroupLoginUrlResponse {

    @JsonAlias("consent_uri")
    private String consentUri;
}

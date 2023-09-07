package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface SessionServerResponse {
    @JsonPath("$.Response.[?(@.Token)].Token")
    Token getToken();

    @JsonPath("$..session_timeout") // We have to deep search because there are multiple types of user (company and person)
    Long getExpiryTimeInSeconds();

    @JsonPath("$.Response.[?(@.UserApiKey)].UserApiKey.id")
    String getBunqId();
}

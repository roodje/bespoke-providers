package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;

public interface CredentialPasswordIp {
    @JsonPath("$.id")
    Long getId();

    @JsonPath("$.created")
    String getCreated();

    @JsonPath("$.updated")
    String getUpdated();

    @JsonPath("$.status")
    String getStatus();

    @JsonPath("$.expiry_time")
    String getExpiryTime();

    @JsonPath("$.token_value")
    String getTokenValue();
}

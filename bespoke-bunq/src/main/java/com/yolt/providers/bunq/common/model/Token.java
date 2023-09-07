package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;

public interface Token {
    @JsonPath("$.id")
    Long getId();
    @JsonPath("$.token")
    String getTokenString();
}

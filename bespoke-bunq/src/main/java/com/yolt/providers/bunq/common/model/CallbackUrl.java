package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;

public interface CallbackUrl {

    @JsonPath("$.id")
    Long getId();

    @JsonPath("$.created")
    String getCreated();
    @JsonPath("$.updated")
    String getUpdated();
    @JsonPath("$.url")
    String getUrl();
}

package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;

public interface Pagination {
    @JsonPath("$.future_url")
    String getFutureUrl();

    @JsonPath("$.newer_url")
    String getNewerUrl();

    @JsonPath("$.older_url")
    String getOlderUrl();
}

package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;

public interface PaginatedResponse {
    @JsonPath("$.Pagination")
    Pagination getPagination();
}

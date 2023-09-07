package com.yolt.providers.bunq.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface IdResponse {
    @JsonPath("$.Response.id")
    String getId();
}

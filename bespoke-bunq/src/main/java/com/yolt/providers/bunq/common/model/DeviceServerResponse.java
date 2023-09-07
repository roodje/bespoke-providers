package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface DeviceServerResponse {
    @JsonPath("$.Response.[?(@.Id)].Id.id")
    Long getId();
}

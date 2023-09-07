package com.yolt.providers.ing.common.auth;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface RedirectUrlResponse {

    @JsonPath("$.location")
    String getLocation();
}

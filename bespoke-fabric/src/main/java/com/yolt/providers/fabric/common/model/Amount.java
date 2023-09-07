package com.yolt.providers.fabric.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Amount {

    @JsonPath("$.amount")
    double getAmount();

    @JsonPath("$.currency")
    String getCurrency();
}

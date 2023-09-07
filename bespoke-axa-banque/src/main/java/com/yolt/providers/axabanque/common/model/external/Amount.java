package com.yolt.providers.axabanque.common.model.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Amount {

    @JsonPath("$.amount")
    double getAmount();

    @JsonPath("$.currency")
    String getCurrency();
}

package com.yolt.providers.monorepogroup.chebancagroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface CustomerIdResponse {

    @JsonPath("$.data.customerid")
    String getCustomerId();
}

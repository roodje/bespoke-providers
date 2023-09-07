package com.yolt.providers.monorepogroup.atruviagroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

/**
 * Body of the JSON response with SCA Status.
 */
@ProjectedPayload
public interface ScaStatusResponse {

    @JsonPath("$.scaStatus")
    ScaStatus getScaStatus();
}


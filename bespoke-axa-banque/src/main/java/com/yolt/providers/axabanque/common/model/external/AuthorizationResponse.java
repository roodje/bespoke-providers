package com.yolt.providers.axabanque.common.model.external;


import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface AuthorizationResponse {

    @JsonPath("$.authorisationIds")
    List<String> getAuthorisationIds();
}

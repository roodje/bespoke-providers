package com.yolt.providers.gruppocedacri.common.dto.fetchdata;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Account {

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.ownerName")
    String getOwnerName();
}

package com.yolt.providers.fabric.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Account {

    @JsonPath("$.bban")
    String getBban();

    @JsonPath("$.bic")
    String getBic();

    @JsonPath("$.cashAccountType")
    String getCashAccountType();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.name")
    String getName();

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.status")
    String getStatus();

}

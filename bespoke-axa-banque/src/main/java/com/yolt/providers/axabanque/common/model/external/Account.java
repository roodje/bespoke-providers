package com.yolt.providers.axabanque.common.model.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Account {

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.name")
    String getName();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.maskedPan")
    String getMaskedPan();

    @JsonPath("$.bic")
    String getBic();

    @JsonPath("$.bban")
    String getBban();

    @JsonPath("$.accountType")
    String getAccountType();

    @JsonPath("$.cashAccountType")
    String getCashAccountType();
}

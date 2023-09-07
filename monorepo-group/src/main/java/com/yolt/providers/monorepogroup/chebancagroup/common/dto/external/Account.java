package com.yolt.providers.monorepogroup.chebancagroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface Account {

    @JsonPath("$.accountId")
    String getAccountId();

    @JsonPath("$.product.code")
    String getProductCode();


    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.name")
    String getName();
}

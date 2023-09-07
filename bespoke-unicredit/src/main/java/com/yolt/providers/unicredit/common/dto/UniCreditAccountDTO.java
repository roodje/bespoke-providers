package com.yolt.providers.unicredit.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface UniCreditAccountDTO {

    @JsonPath("$.resourceId")
    String getResourceId();

    @JsonPath("$.product")
    String getProduct();

    @JsonPath("$.iban")
    String getIban();

    @JsonPath("$.currency")
    String getCurrency();

    @JsonPath("$.name")
    String getName();

    @JsonPath("$.cashAccountType")
    String getCashAccountType();

    @JsonPath("$.bic")
    String getBic();

    @JsonPath("$.balance")
    List<UniCreditBalanceDTO> getBalances();

    @JsonPath("$._links.next.href")
    String getNextPageUrl();
}

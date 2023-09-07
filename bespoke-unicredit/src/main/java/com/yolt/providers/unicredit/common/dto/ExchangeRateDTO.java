package com.yolt.providers.unicredit.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

@ProjectedPayload
public interface ExchangeRateDTO {

    @JsonPath("$.currencyFrom")
    String getCurrencyFrom();

    @JsonPath("$.rateFrom")
    String getRateFrom();

    @JsonPath("$.currencyTo")
    String getCurrencyTo();

    @JsonPath("$.rateTo")
    String getRateTo();

    @JsonPath("$.rateDate")
    String getRateDate();

    @JsonPath("$.rateContract")
    String getRateContract();
}
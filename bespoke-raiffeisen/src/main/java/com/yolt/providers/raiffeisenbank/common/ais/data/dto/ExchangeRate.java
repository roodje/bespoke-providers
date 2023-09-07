package com.yolt.providers.raiffeisenbank.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.time.LocalDate;

@ProjectedPayload
public interface ExchangeRate {
    @JsonPath("$.sourceCurrency.CurrencyCode")
    String getSourceCurrency();

    @JsonPath("$.exchangeRate")
    String getExchangeRate();

    @JsonPath("$.targetCurrency.CurrencyCode")
    String getTargetCurrency();

    @JsonPath("$.quotationDate")
    LocalDate getQuotationDate();

    @JsonPath("$.contractIdentification")
    String getContractIdentification();

}

package com.yolt.providers.redsys.common.dto;

import lombok.Data;

@Data
public class ReportExchangeRate {
    private String sourceCurrency;

    private String exchangeRate;

    private String unitCurrency;

    private String targetCurrency;

    private String quotationDate;

    private String contractIdentification;
}

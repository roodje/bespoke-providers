package com.yolt.providers.raiffeisenbank.common.ais.data.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@ProjectedPayload
public interface Transaction {

    @JsonPath("$.transactionId")
    String getTransactionId();

    @JsonPath("$.endToEndId")
    String getEndToEndId();

    @JsonPath("$.mandateId")
    String getMandateId();

    @JsonPath("$.bookingDate")
    LocalDate getBookingDate();

    @JsonPath("$.valueDate")
    LocalDate getValueDate();

    @JsonPath("$.transactionAmount.amount")
    BigDecimal getAmount();

    @JsonPath("$.transactionAmount.currency")
    String getCurrency();

    @JsonPath("$.exchangeRate")
    List<ExchangeRate> getExchangeRate();

    @JsonPath("$.creditorName")
    String getCreditorName();

    @JsonPath("$.creditorAccount.iban")
    String getCreditorIban();

    @JsonPath("$.ultimateCreditor")
    String getUltimateCreditor();

    @JsonPath("$.debtorName")
    String getDebtorName();

    @JsonPath("$.debtorAccount.iban")
    String getDebtorIban();

    @JsonPath("$.ultimateDebtor")
    String getUltimateDebtor();

    @JsonPath("$.remittanceInformationUnstructured")
    String getRemittanceInformationUnstructured();

    @JsonPath("$.remittanceInformationStructured")
    String getRemittanceInformationStructured();

    @JsonPath("$.purposeCode")
    String getPurposeCode();

    @JsonPath("$.bankTransactionCode")
    String getBankTransactionCode();
}

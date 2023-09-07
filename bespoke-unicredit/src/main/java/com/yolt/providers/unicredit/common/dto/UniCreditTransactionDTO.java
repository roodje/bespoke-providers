package com.yolt.providers.unicredit.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface UniCreditTransactionDTO {

    @JsonPath("$.transactionId")
    String getTransactionId();

    @JsonPath("$.entryReference")
    String getEntryReference();

    @JsonPath("$.endToEndId")
    String getEndToEndId();

    @JsonPath("$.mandateId")
    String getMandateId();

    @JsonPath("$.checkId")
    String getCheckId();

    @JsonPath("$.creditorId")
    String getCreditorId();

    @JsonPath("$.bookingDate")
    String getBookingDate();

    @JsonPath("$.valueDate")
    String getValueDate();

    @JsonPath("$.transactionAmount.amount")
    double getAmount();

    @JsonPath("$.transactionAmount.currency")
    String getCurrency();

    @JsonPath("$.creditorName")
    String getCreditorName();

    @JsonPath("$.creditorAccount.iban")
    String getCreditorIban();

    @JsonPath("$.creditorAccount.currency")
    String getCreditorCurrency();

    @JsonPath("$.ultimateCreditor")
    String getUltimateCreditor();

    @JsonPath("$.debtorName")
    String getDebtorName();

    @JsonPath("$.debtorAccount.iban")
    String getDebtorIban();

    @JsonPath("$.debtorAccount.currency")
    String getDebtorCurrency();

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

    @JsonPath("$.proprietaryBankTransactionCode")
    String getProprietaryBankTransactionCode();

    @JsonPath("$.exchangeRate")
    List<ExchangeRateDTO> getExchangeRates();

    @JsonPath("$.additionalInformation")
    String getAdditionalInformation();

}

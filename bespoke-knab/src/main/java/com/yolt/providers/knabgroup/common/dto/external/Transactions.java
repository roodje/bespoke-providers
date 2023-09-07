package com.yolt.providers.knabgroup.common.dto.external;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.util.List;

@ProjectedPayload
public interface Transactions {

    @JsonPath("$.transactions.booked")
    List<Transaction> getTransactions();

    interface Transaction {

        @JsonPath("$.transactionId")
        String getTransactionId();

        @JsonPath("$.transactionAmount.currency")
        String getCurrency();

        @JsonPath("$.transactionAmount.amount")
        BigDecimal getAmount();

        @JsonPath("$.creditorAccount")
        TargetAccount getCreditorAccount();

        @JsonPath("$.debtorAccount")
        TargetAccount getDebtorAccount();

        @JsonPath("$.entryReference")
        String getEntryReference();

        @JsonPath("$.bookingDate")
        String getBookingDate();

        @JsonPath("$.valueDate")
        String getValueDate();

        @JsonPath("$.transactionDate")
        String getTransactionDate();

        @JsonPath("$.creditorName")
        String getCreditorName();

        @JsonPath("$.debtorName")
        String getDebtorName();

        @JsonPath("$.remittanceInformationUnstructured")
        String getRemittanceInformationUnstructured();

        @JsonPath("$.remittanceInformationStructured")
        String getRemittanceInformationStructured();

        @JsonPath("$.endToEndId")
        String getEndToEndId();

        @JsonPath("$.mandateId")
        String getMandateId();

        @JsonPath("$.creditorId")
        String getCreditorId();

        @JsonPath("$.proprietaryBankTransactionCode")
        String getProprietaryBankTransactionCode();

        @JsonPath("$.exchangeRate")
        List<ExchangeRate> getExchangeRate();

        @JsonPath("$.dayStartBalance")
        DayStartBalance getDayStartBalance();

    }

    interface TargetAccount {

        @JsonPath("$.iban")
        String getIban();

        @JsonPath("$.bban")
        String getBban();
    }

    interface ExchangeRate {

        @JsonPath("$.currencyFrom")
        String getCurrencyFrom();

        @JsonPath("$.rate")
        String getRate();

        @JsonPath("$.currencyTo")
        String getCurrencyTo();

        @JsonPath("$.rateDate")
        String getRateDate();

        @JsonPath("$.rateContract")
        String getContract();
    }

    interface DayStartBalance {

        @JsonPath("$.amount")
        String getAmount();

        @JsonPath("$.currency")
        String getCurrency();
    }
}
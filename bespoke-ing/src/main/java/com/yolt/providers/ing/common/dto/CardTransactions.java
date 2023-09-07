package com.yolt.providers.ing.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.util.List;

@ProjectedPayload
public interface CardTransactions {

    @JsonPath("$.cardTransactions.booked")
    List<CardTransaction> getBookedTransactions();

    @JsonPath("$.cardTransactions.pending")
    List<CardTransaction> getPendingTransactions();

    @JsonPath("$.cardTransactions._links.next.href")
    String getNextPageUrl();

    interface CardTransaction {

        @JsonPath("$.cardTransactionId")
        String getCardTransactionId();

        @JsonPath("$.transactionDate")
        String getTransactionDate();

        @JsonPath("$.bookingDate")
        String getBookingDate();

        @JsonPath("$.transactionAmount.amount")
        BigDecimal getAmount();

        @JsonPath("$.transactionAmount.currency")
        String getCurrency();

        @JsonPath("$.maskedPan")
        String getMaskedPan();

        @JsonPath("$.transactionDetails")
        String getDescription();
    }
}
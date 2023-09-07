package com.yolt.providers.unicredit.common.dto;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface UniCreditTransactionsDTO {

    @JsonPath("$.account.iban")
    String getAccountIban();

    @JsonPath("$.account.currency")
    String getAccountCurrency();

    @JsonPath("$.transactions.booked")
    List<UniCreditTransactionDTO> getBookedTransactions();

    @JsonPath("$.transactions.pending")
    List<UniCreditTransactionDTO> getPendingTransactions();

    @JsonPath("$.transactions._links.next.href")
    String getNextPageUrl();

}

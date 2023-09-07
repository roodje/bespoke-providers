package com.yolt.providers.belfius.common.model.ais;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.math.BigDecimal;
import java.util.List;

@ProjectedPayload
public interface TransactionResponse {

    @JsonPath("$._embedded.transactions")
    List<Transaction> getTransactions();

    @JsonPath("$._embedded.next_page_key")
    String getNextPageUrl();

    interface Transaction {

        @JsonPath("$.transaction_ref")
        String getTransactionRef();

        @JsonPath("$.amount")
        BigDecimal getAmount();

        @JsonPath("$.currency")
        String getCurrency();

        @JsonPath("$.execution_date")
        String getExecutionDateTime();

        @JsonPath("$.value_date")
        String getValueDate();

        @JsonPath("$.counterparty_account")
        String getCounterPartyAccount();

        @JsonPath("$.counterparty_info")
        String getCounterPartyInfo();

        @JsonPath("$.communication")
        String getCommunication();

        @JsonPath("$.communication_type")
        String getCommunicationType();

        @JsonPath("$.remittance_info")
        String getRemittanceInfo();
    }
}

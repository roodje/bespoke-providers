package com.yolt.providers.bunq.common.model;

import org.springframework.data.web.JsonPath;
import org.springframework.data.web.ProjectedPayload;

import java.util.List;

@ProjectedPayload
public interface TransactionsResponse extends PaginatedResponse {
    @JsonPath("$.Response")
    List<Transaction> getTransactions();

    interface Transaction {
        @JsonPath("$.Payment.id")
        String getTransactionId();

        @JsonPath("$.Payment.created")
        // @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS") // XXX This does should parse the field to LocalDateTime but can't get it to work
        String getCreated();

        @JsonPath("$.Payment.amount")
        Amount getAmount();

        @JsonPath("$.Payment.status")
        String getStatus();

        @JsonPath("$.Payment.type")
        String getType();

        @JsonPath("$.Payment.description")
        String getDescription();

        @JsonPath("$.Payment.merchant_reference")
        String getMerchant();

        @JsonPath("$.Payment.alias.iban")
        String getAliasIban();

        @JsonPath("$.Payment.alias.display_name")
        String getAliasDisplayName();

        @JsonPath("$.Payment.counterparty_alias.iban")
        String getCounterpartyAliasIban();

        @JsonPath("$.Payment.counterparty_alias.display_name")
        String getCounterpartyAliasDisplayName();
    }
}

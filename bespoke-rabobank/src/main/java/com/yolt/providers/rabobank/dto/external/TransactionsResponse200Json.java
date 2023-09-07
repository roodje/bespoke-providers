package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;

/**
 * Body of the JSON response for a successful read transaction list request.
 */
public class TransactionsResponse200Json {
    @JsonProperty("account")
    private AccountReference account = null;

    @JsonProperty("transactions")
    private AccountReport transactions = null;

    public TransactionsResponse200Json account(AccountReference account) {
        this.account = account;
        return this;
    }

    /**
     * Get account
     *
     * @return account
     **/
    @Valid
    public AccountReference getAccount() {
        return account;
    }

    public void setAccount(AccountReference account) {
        this.account = account;
    }

    public TransactionsResponse200Json transactions(AccountReport transactions) {
        this.transactions = transactions;
        return this;
    }

    /**
     * Get transactions
     *
     * @return transactions
     **/
    @Valid
    public AccountReport getTransactions() {
        return transactions;
    }

    public void setTransactions(AccountReport transactions) {
        this.transactions = transactions;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionsResponse200Json transactionsResponse200Json = (TransactionsResponse200Json) o;
        return Objects.equals(this.account, transactionsResponse200Json.account) &&
                Objects.equals(this.transactions, transactionsResponse200Json.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, transactions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TransactionsResponse200Json {\n");

        sb.append("    account: ").append(toIndentedString(account)).append("\n");
        sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}


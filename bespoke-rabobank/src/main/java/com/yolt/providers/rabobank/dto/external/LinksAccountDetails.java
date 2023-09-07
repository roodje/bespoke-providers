package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;

/**
 * Links to the account, which can be directly used for retrieving account information from this dedicated account. Links to \&quot;balances\&quot; and/or \&quot;transactions\&quot; These links are only supported, when the corresponding consent has been already granted.
 */
public class LinksAccountDetails {
    @JsonProperty("account")
    private LinksHref account = null;

    @JsonProperty("balances")
    private LinksHref balances = null;

    @JsonProperty("transactions")
    private LinksHref transactions = null;

    public LinksAccountDetails account(LinksHref account) {
        this.account = account;
        return this;
    }

    /**
     * Get account
     *
     * @return account
     **/
    @Valid
    public LinksHref getAccount() {
        return account;
    }

    public void setAccount(LinksHref account) {
        this.account = account;
    }

    public LinksAccountDetails balances(LinksHref balances) {
        this.balances = balances;
        return this;
    }

    /**
     * Get balances
     *
     * @return balances
     **/
    @Valid
    public LinksHref getBalances() {
        return balances;
    }

    public void setBalances(LinksHref balances) {
        this.balances = balances;
    }

    public LinksAccountDetails transactions(LinksHref transactions) {
        this.transactions = transactions;
        return this;
    }

    /**
     * Get transactions
     *
     * @return transactions
     **/
    @Valid
    public LinksHref getTransactions() {
        return transactions;
    }

    public void setTransactions(LinksHref transactions) {
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
        LinksAccountDetails linksAccountDetails = (LinksAccountDetails) o;
        return Objects.equals(this.account, linksAccountDetails.account) &&
                Objects.equals(this.balances, linksAccountDetails.balances) &&
                Objects.equals(this.transactions, linksAccountDetails.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, balances, transactions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinksAccountDetails {\n");

        sb.append("    account: ").append(toIndentedString(account)).append("\n");
        sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
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


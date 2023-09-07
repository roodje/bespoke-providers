package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body of the response for a successful read balance request.
 */
public class ReadBalanceResponse {

    @JsonProperty("account")
    private AccountReferenceIban account = null;

    @JsonProperty("balances")
    private BalanceList balances = null;

    public ReadBalanceResponse account(AccountReferenceIban account) {
        this.account = account;
        return this;
    }

    /**
     * Get account
     *
     * @return account
     **/
    @Valid
    public AccountReferenceIban getAccount() {
        return account;
    }

    public void setAccount(AccountReferenceIban account) {
        this.account = account;
    }

    public ReadBalanceResponse balances(BalanceList balances) {
        this.balances = balances;
        return this;
    }

    /**
     * Get balances
     *
     * @return balances
     **/
    @NotNull
    @Valid
    public BalanceList getBalances() {
        return balances;
    }

    public void setBalances(BalanceList balances) {
        this.balances = balances;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadBalanceResponse readBalanceResponse200 = (ReadBalanceResponse) o;
        return Objects.equals(this.account, readBalanceResponse200.account) &&
                Objects.equals(this.balances, readBalanceResponse200.balances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, balances);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ReadBalanceResponse200 {\n");

        sb.append("    account: ").append(toIndentedString(account)).append("\n");
        sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
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


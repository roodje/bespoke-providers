package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Creditor account.
 */
public class CreditorAccount {
    @JsonProperty("currency")
    private String currency = null;

    @JsonProperty("iban")
    private String iban = null;

    public CreditorAccount currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Currency of the creditor account.
     *
     * @return currency
     **/
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public CreditorAccount iban(String iban) {
        this.iban = iban;
        return this;
    }

    /**
     * Creditor account of type IBAN2007Identifier.
     *
     * @return iban
     **/
    @NotNull
    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CreditorAccount creditorAccount = (CreditorAccount) o;
        return Objects.equals(this.currency, creditorAccount.currency) &&
                Objects.equals(this.iban, creditorAccount.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, iban);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CreditorAccount {\n");

        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
        sb.append("    iban: ").append(toIndentedString(iban)).append("\n");
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


package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * AccountReference
 */
public class AccountReference {
    @JsonProperty("bban")
    private String bban = null;

    @JsonProperty("currency")
    private String currency = null;

    @JsonProperty("iban")
    private String iban = null;

    public AccountReference bban(String bban) {
        this.bban = bban;
        return this;
    }

    /**
     * Basic Bank Account Number (BBAN) Identifier
     *
     * @return bban
     **/
    @Pattern(regexp = "[a-zA-Z0-9]{1,30}")
    public String getBban() {
        return bban;
    }

    public void setBban(String bban) {
        this.bban = bban;
    }

    public AccountReference currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Currency of the transaction amount.
     *
     * @return currency
     **/
    @Pattern(regexp = "[A-Z]{3}")
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public AccountReference iban(String iban) {
        this.iban = iban;
        return this;
    }

    /**
     * IBAN of an account
     *
     * @return iban
     **/
    @Pattern(regexp = "[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}")
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
        AccountReference accountReference = (AccountReference) o;
        return Objects.equals(this.bban, accountReference.bban) &&
                Objects.equals(this.currency, accountReference.currency) &&
                Objects.equals(this.iban, accountReference.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bban, currency, iban);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReference {\n");

        sb.append("    bban: ").append(toIndentedString(bban)).append("\n");
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


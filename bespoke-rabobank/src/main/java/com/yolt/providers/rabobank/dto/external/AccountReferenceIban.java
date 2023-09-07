package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * Reference to an account by the Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements.
 */
public class AccountReferenceIban {
    @JsonProperty("currency")
    private String currency = null;

    @JsonProperty("iban")
    private String iban = null;

    public AccountReferenceIban currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * ISO 4217 Alpha 3 currency code
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

    public AccountReferenceIban iban(String iban) {
        this.iban = iban;
        return this;
    }

    /**
     * IBAN of an account
     *
     * @return iban
     **/
    @NotNull
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
        AccountReferenceIban accountReferenceIban = (AccountReferenceIban) o;
        return Objects.equals(this.currency, accountReferenceIban.currency) &&
                Objects.equals(this.iban, accountReferenceIban.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currency, iban);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountReferenceIban {\n");

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


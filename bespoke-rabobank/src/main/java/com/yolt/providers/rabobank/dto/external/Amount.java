package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * Amount
 */
public class Amount {
    @JsonProperty("amount")
    private String amount = null;

    @JsonProperty("currency")
    private String currency = null;

    public Amount amount(String amount) {
        this.amount = amount;
        return this;
    }

    /**
     * The amount given with fractional digits, where fractions must be compliant to the currency definition. Up to 14 significant figures. Negative amounts are signed by minus. The decimal separator is a dot.   **Example:** Valid representations for EUR with up to two decimals are:      * 1056    * 5768.2    * -1.50    * 5877.78
     *
     * @return amount
     **/
    @NotNull
    @Pattern(regexp = "-?[0-9]{1,14}(\\.[0-9]{1,3})?")
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public Amount currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * ISO 4217 Alpha 3 currency code
     *
     * @return currency
     **/
    @NotNull
    @Pattern(regexp = "[A-Z]{3}")
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Amount amount = (Amount) o;
        return Objects.equals(this.amount, amount.amount) &&
                Objects.equals(this.currency, amount.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Amount {\n");

        sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
        sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
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


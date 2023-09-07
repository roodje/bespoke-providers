package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Original amount (f.e. USD amount paid towards the EUR DbtrAcct).
 */
public class InstructedAmountType {
    @JsonProperty("amount")
    private BigDecimal amount = null;

    @JsonProperty("sourceCurrency")
    private String sourceCurrency = null;

    public InstructedAmountType amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Amount as instructed by the initiator of the transaction in the currency as instructed (before exchange if applicable). Decimal separator is a dot (.).
     *
     * @return amount
     **/
    @Valid
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public InstructedAmountType sourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
        return this;
    }

    /**
     * Currency of instructed amount
     *
     * @return sourceCurrency
     **/
    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstructedAmountType instructedAmountType = (InstructedAmountType) o;
        return Objects.equals(this.amount, instructedAmountType.amount) &&
                Objects.equals(this.sourceCurrency, instructedAmountType.sourceCurrency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, sourceCurrency);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InstructedAmountType {\n");

        sb.append("    amount: ").append(toIndentedString(amount)).append("\n");
        sb.append("    sourceCurrency: ").append(toIndentedString(sourceCurrency)).append("\n");
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


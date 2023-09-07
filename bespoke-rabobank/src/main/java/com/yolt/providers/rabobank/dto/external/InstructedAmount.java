package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Instructed amount for given currency.
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-08-02T10:19:56.414332200+02:00[Europe/Warsaw]")

public class InstructedAmount {
    @JsonProperty("content")
    private String content = null;

    @JsonProperty("currency")
    private String currency = null;

    public InstructedAmount content(String content) {
        this.content = content;
        return this;
    }

    /**
     * Amount. Decimal character is a dot.
     *
     * @return content
     **/
    @NotNull
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public InstructedAmount currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Currency of the instructed amount.
     *
     * @return currency
     **/
    @NotNull
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
        InstructedAmount instructedAmount = (InstructedAmount) o;
        return Objects.equals(this.content, instructedAmount.content) &&
                Objects.equals(this.currency, instructedAmount.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, currency);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class InstructedAmount {\n");

        sb.append("    content: ").append(toIndentedString(content)).append("\n");
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


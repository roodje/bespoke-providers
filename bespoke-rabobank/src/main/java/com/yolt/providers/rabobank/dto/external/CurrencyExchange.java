package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

/**
 * Exchange Rate
 */
public class CurrencyExchange {
    @JsonProperty("sourceCurrency")
    private String sourceCurrency = null;

    @JsonProperty("targetCurrency")
    private String targetCurrency = null;

    @JsonProperty("exchangeRate")
    private String exchangeRate = null;

    public CurrencyExchange sourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
        return this;
    }

    /**
     * Currency of the transaction amount.
     *
     * @return sourceCurrency
     **/
    @NotNull
    @Pattern(regexp = "[A-Z]{3}")
    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public CurrencyExchange targetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
        return this;
    }

    /**
     * Currency of the transaction amount.
     *
     * @return targetCurrency
     **/
    @NotNull
    @Pattern(regexp = "[A-Z]{3}")
    public String getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(String targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public CurrencyExchange exchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
        return this;
    }

    /**
     * Exchange rate of the currency exchange.
     *
     * @return exchangeRate
     **/
    @NotNull
    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CurrencyExchange currencyExchange = (CurrencyExchange) o;
        return Objects.equals(this.sourceCurrency, currencyExchange.sourceCurrency) &&
                Objects.equals(this.targetCurrency, currencyExchange.targetCurrency) &&
                Objects.equals(this.exchangeRate, currencyExchange.exchangeRate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceCurrency, targetCurrency, exchangeRate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CurrencyExchange {\n");

        sb.append("    sourceCurrency: ").append(toIndentedString(sourceCurrency)).append("\n");
        sb.append("    targetCurrency: ").append(toIndentedString(targetCurrency)).append("\n");
        sb.append("    exchangeRate: ").append(toIndentedString(exchangeRate)).append("\n");
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


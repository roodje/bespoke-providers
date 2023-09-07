package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * A single balance element
 */
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-08-02T10:19:56.345369500+02:00[Europe/Warsaw]")
public class Balance {
    @JsonProperty("balanceAmount")
    private Amount balanceAmount = null;

    @JsonProperty("balanceType")
    private BalanceType balanceType = null;

    @JsonProperty("lastChangeDateTime")
    private OffsetDateTime lastChangeDateTime = null;

    public Balance balanceAmount(Amount balanceAmount) {
        this.balanceAmount = balanceAmount;
        return this;
    }

    /**
     * Get balanceAmount
     *
     * @return balanceAmount
     **/
    @NotNull
    @Valid
    public Amount getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(Amount balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public Balance balanceType(BalanceType balanceType) {
        this.balanceType = balanceType;
        return this;
    }

    /**
     * Get balanceType
     *
     * @return balanceType
     **/
    @NotNull
    @Valid
    public BalanceType getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(BalanceType balanceType) {
        this.balanceType = balanceType;
    }

    public Balance lastChangeDateTime(OffsetDateTime lastChangeDateTime) {
        this.lastChangeDateTime = lastChangeDateTime;
        return this;
    }

    /**
     * Get lastChangeDateTime
     *
     * @return lastChangeDateTime
     **/
    @Valid
    public OffsetDateTime getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public void setLastChangeDateTime(OffsetDateTime lastChangeDateTime) {
        this.lastChangeDateTime = lastChangeDateTime;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Balance balance = (Balance) o;
        return Objects.equals(this.balanceAmount, balance.balanceAmount) &&
                Objects.equals(this.balanceType, balance.balanceType) &&
                Objects.equals(this.lastChangeDateTime, balance.lastChangeDateTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(balanceAmount, balanceType, lastChangeDateTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Balance {\n");

        sb.append("    balanceAmount: ").append(toIndentedString(balanceAmount)).append("\n");
        sb.append("    balanceType: ").append(toIndentedString(balanceType)).append("\n");
        sb.append("    lastChangeDateTime: ").append(toIndentedString(lastChangeDateTime)).append("\n");
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


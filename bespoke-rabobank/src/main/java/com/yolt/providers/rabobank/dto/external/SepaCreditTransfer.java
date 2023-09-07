package com.yolt.providers.rabobank.dto.external;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import java.util.Objects;

/**
 * Sepa Credit Transfer information.
 */
public class SepaCreditTransfer extends CreditTransfer {
    @JsonProperty("remittanceInformationStructured")
    private RemittanceInformationStructured remittanceInformationStructured = null;

    public SepaCreditTransfer remittanceInformationStructured(RemittanceInformationStructured remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
        return this;
    }

    /**
     * Get remittanceInformationStructured
     *
     * @return remittanceInformationStructured
     **/
    @Valid
    public RemittanceInformationStructured getRemittanceInformationStructured() {
        return remittanceInformationStructured;
    }

    public void setRemittanceInformationStructured(RemittanceInformationStructured remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SepaCreditTransfer sepaCreditTransfer = (SepaCreditTransfer) o;
        return Objects.equals(this.remittanceInformationStructured, sepaCreditTransfer.remittanceInformationStructured) &&
                super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(remittanceInformationStructured, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SepaCreditTransfer {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    remittanceInformationStructured: ").append(toIndentedString(remittanceInformationStructured)).append("\n");
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


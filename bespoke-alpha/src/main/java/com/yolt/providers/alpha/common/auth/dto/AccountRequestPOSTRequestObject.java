package com.yolt.providers.alpha.common.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Allows setup of an account access request
 */
@ApiModel(description = "Allows setup of an account access request")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-07-18T15:36:09.705982700+02:00[Europe/Warsaw]")

public class AccountRequestPOSTRequestObject {
    @JsonProperty("ProductIdentifiers")
    private ProductIdentifiers productIdentifiers = null;

    @JsonProperty("Risk")
    private Risk risk = null;

    public AccountRequestPOSTRequestObject productIdentifiers(ProductIdentifiers productIdentifiers) {
        this.productIdentifiers = productIdentifiers;
        return this;
    }

    /**
     * Get productIdentifiers
     *
     * @return productIdentifiers
     **/
    @ApiModelProperty(value = "")

    @Valid

    public ProductIdentifiers getProductIdentifiers() {
        return productIdentifiers;
    }

    public void setProductIdentifiers(ProductIdentifiers productIdentifiers) {
        this.productIdentifiers = productIdentifiers;
    }

    public AccountRequestPOSTRequestObject risk(Risk risk) {
        this.risk = risk;
        return this;
    }

    /**
     * Get risk
     *
     * @return risk
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid

    public Risk getRisk() {
        return risk;
    }

    public void setRisk(Risk risk) {
        this.risk = risk;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountRequestPOSTRequestObject accountRequestPOSTRequestObject = (AccountRequestPOSTRequestObject) o;
        return Objects.equals(this.productIdentifiers, accountRequestPOSTRequestObject.productIdentifiers) &&
                Objects.equals(this.risk, accountRequestPOSTRequestObject.risk);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productIdentifiers, risk);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AccountRequestPOSTRequestObject {\n");

        sb.append("    productIdentifiers: ").append(toIndentedString(productIdentifiers)).append("\n");
        sb.append("    risk: ").append(toIndentedString(risk)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}


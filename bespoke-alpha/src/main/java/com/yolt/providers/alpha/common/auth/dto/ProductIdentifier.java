package com.yolt.providers.alpha.common.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Identifies a Bank product owned by the end user
 */
@ApiModel(description = "Identifies a Bank product owned by the end user")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-07-18T15:36:09.705982700+02:00[Europe/Warsaw]")

public class ProductIdentifier {
    @JsonProperty("AccountCode")
    private String accountCode = null;
    @JsonProperty("Scheme")
    private SchemeEnum scheme = null;

    public ProductIdentifier accountCode(String accountCode) {
        this.accountCode = accountCode;
        return this;
    }

    /**
     * the global identifier of the product (e.g. IBAN, Card number)
     *
     * @return accountCode
     **/
    @ApiModelProperty(required = true, value = "the global identifier of the product (e.g. IBAN, Card number)")
    @NotNull


    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public ProductIdentifier scheme(SchemeEnum scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Name of the identification scheme (AccountCode scheme)
     *
     * @return scheme
     **/
    @ApiModelProperty(required = true, value = "Name of the identification scheme (AccountCode scheme)")
    @NotNull


    public SchemeEnum getScheme() {
        return scheme;
    }

    public void setScheme(SchemeEnum scheme) {
        this.scheme = scheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ProductIdentifier productIdentifier = (ProductIdentifier) o;
        return Objects.equals(this.accountCode, productIdentifier.accountCode) &&
                Objects.equals(this.scheme, productIdentifier.scheme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountCode, scheme);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ProductIdentifier {\n");

        sb.append("    accountCode: ").append(toIndentedString(accountCode)).append("\n");
        sb.append("    scheme: ").append(toIndentedString(scheme)).append("\n");
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

    /**
     * Name of the identification scheme (AccountCode scheme)
     */
    public enum SchemeEnum {
        NA("NA"),

        ACCOUNT("Account"),

        CARD("Card");

        private String value;

        SchemeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static SchemeEnum fromValue(String text) {
            for (SchemeEnum b : SchemeEnum.values()) {
                if (String.valueOf(b.value).equals(text)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + text + "'");
        }

        @Override
        @JsonValue
        public String toString() {
            return String.valueOf(value);
        }
    }
}


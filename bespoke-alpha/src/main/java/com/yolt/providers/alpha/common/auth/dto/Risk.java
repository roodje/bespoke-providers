package com.yolt.providers.alpha.common.auth.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Risk assessment related information
 */
@ApiModel(description = "Risk assessment related information")
@javax.annotation.Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2022-07-18T15:36:09.705982700+02:00[Europe/Warsaw]")

public class Risk {
    @JsonProperty("PaymentContextCode")
    private PaymentContextCodeEnum paymentContextCode = null;
    @JsonProperty("MerchantCategoryCode")
    private String merchantCategoryCode = null;
    @JsonProperty("MerchantCustomerIdentification")
    private String merchantCustomerIdentification = null;
    @JsonProperty("DeliveryAddress")
    private RiskDeliveryAddress deliveryAddress = null;

    public Risk paymentContextCode(PaymentContextCodeEnum paymentContextCode) {
        this.paymentContextCode = paymentContextCode;
        return this;
    }

    /**
     * Specifies the payment context
     *
     * @return paymentContextCode
     **/
    @ApiModelProperty(value = "Specifies the payment context")


    public PaymentContextCodeEnum getPaymentContextCode() {
        return paymentContextCode;
    }

    public void setPaymentContextCode(PaymentContextCodeEnum paymentContextCode) {
        this.paymentContextCode = paymentContextCode;
    }

    public Risk merchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
        return this;
    }

    /**
     * Category code conforms to ISO 18245, related to the type of services or goods the merchant provides for the transaction
     *
     * @return merchantCategoryCode
     **/
    @ApiModelProperty(value = "Category code conforms to ISO 18245, related to the type of services or goods the merchant provides for the transaction")

    @Size(min = 3, max = 4)
    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public void setMerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public Risk merchantCustomerIdentification(String merchantCustomerIdentification) {
        this.merchantCustomerIdentification = merchantCustomerIdentification;
        return this;
    }

    /**
     * The unique customer identifier of the PSU with the merchant.
     *
     * @return merchantCustomerIdentification
     **/
    @ApiModelProperty(value = "The unique customer identifier of the PSU with the merchant.")

    @Size(min = 1, max = 70)
    public String getMerchantCustomerIdentification() {
        return merchantCustomerIdentification;
    }

    public void setMerchantCustomerIdentification(String merchantCustomerIdentification) {
        this.merchantCustomerIdentification = merchantCustomerIdentification;
    }

    public Risk deliveryAddress(RiskDeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
        return this;
    }

    /**
     * Get deliveryAddress
     *
     * @return deliveryAddress
     **/
    @ApiModelProperty(value = "")

    @Valid

    public RiskDeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(RiskDeliveryAddress deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Risk risk = (Risk) o;
        return Objects.equals(this.paymentContextCode, risk.paymentContextCode) &&
                Objects.equals(this.merchantCategoryCode, risk.merchantCategoryCode) &&
                Objects.equals(this.merchantCustomerIdentification, risk.merchantCustomerIdentification) &&
                Objects.equals(this.deliveryAddress, risk.deliveryAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentContextCode, merchantCategoryCode, merchantCustomerIdentification, deliveryAddress);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Risk {\n");

        sb.append("    paymentContextCode: ").append(toIndentedString(paymentContextCode)).append("\n");
        sb.append("    merchantCategoryCode: ").append(toIndentedString(merchantCategoryCode)).append("\n");
        sb.append("    merchantCustomerIdentification: ").append(toIndentedString(merchantCustomerIdentification)).append("\n");
        sb.append("    deliveryAddress: ").append(toIndentedString(deliveryAddress)).append("\n");
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
     * Specifies the payment context
     */
    public enum PaymentContextCodeEnum {
        BILLPAYMENT("BillPayment"),

        ECOMMERCEGOODS("EcommerceGoods"),

        ECOMMERCESERVICES("EcommerceServices"),

        OTHER("Other"),

        PERSONTOPERSON("PersonToPerson");

        private String value;

        PaymentContextCodeEnum(String value) {
            this.value = value;
        }

        @JsonCreator
        public static PaymentContextCodeEnum fromValue(String text) {
            for (PaymentContextCodeEnum b : PaymentContextCodeEnum.values()) {
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


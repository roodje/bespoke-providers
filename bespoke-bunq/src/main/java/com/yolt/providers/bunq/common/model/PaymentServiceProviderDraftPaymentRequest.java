package com.yolt.providers.bunq.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@Builder
@AllArgsConstructor
public class PaymentServiceProviderDraftPaymentRequest {

    @JsonProperty("sender_iban")
    private String senderIban;
    @JsonProperty("sender_name")
    private String senderName;
    @JsonProperty("counterparty_iban")
    private String counterpartyIban;
    @JsonProperty("counterparty_name")
    private String counterpartyName;
    @JsonProperty("description")
    private String description;
    @JsonProperty("amount")
    private PaymentAmount amount;
    @JsonProperty("status")
    private String status;

    public void validate() {
        if (StringUtils.isEmpty(this.senderIban)) {
            throw new IllegalArgumentException("Debtor account is required");
        }
        if (StringUtils.isEmpty(this.counterpartyIban) || StringUtils.isEmpty(this.counterpartyName)) {
            throw new IllegalArgumentException("Creditor name and account are required");
        }
        if (StringUtils.isEmpty(this.description)) {
            throw new IllegalArgumentException("Description is required");
        }
        this.amount.validate();
    }
}

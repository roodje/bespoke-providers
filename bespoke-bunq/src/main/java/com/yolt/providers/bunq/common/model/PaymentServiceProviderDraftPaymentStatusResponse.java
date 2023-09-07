package com.yolt.providers.bunq.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;

@Getter
@AllArgsConstructor
public class PaymentServiceProviderDraftPaymentStatusResponse {

    public static final String PAYMENT_SERVICE_PROVIDER_DRAFT_PAYMENT_FIELD_NAME = "PaymentServiceProviderDraftPayment";
    public static final String STATUS_FIELD_NAME = "status";
    private PaymentStatusValue status;

    @JsonProperty("Response")
    private void unpackStatusFromNestedObject(JsonNode jsonNode) {
        if (ObjectUtils.isNotEmpty(jsonNode) && ObjectUtils.isNotEmpty(jsonNode.get(PAYMENT_SERVICE_PROVIDER_DRAFT_PAYMENT_FIELD_NAME))) {
            JsonNode draftPaymentNode = jsonNode.get(PAYMENT_SERVICE_PROVIDER_DRAFT_PAYMENT_FIELD_NAME);
            String statusString = draftPaymentNode.get(STATUS_FIELD_NAME).asText();
            status = PaymentStatusValue.valueOf(statusString);
        }

    }


}

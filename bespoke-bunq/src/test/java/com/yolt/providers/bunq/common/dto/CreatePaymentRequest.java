package com.yolt.providers.bunq.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    @JsonProperty("amount")
    Amount amount = new Amount();

    @JsonProperty("counterparty_alias")
    Counterparty counteryparty = new Counterparty();

    @JsonProperty("description")
    String description = "testPayment";

    @Data
    class Amount {
        @JsonProperty("value")
        String value = "20.00";
        @JsonProperty("currency")
        String currency = "EUR";
    }

    @Data
    class Counterparty {
        @JsonProperty("type")
        String type = "IBAN";

        String value = "NL23ABNA0581113566";

        String name = "duder";
    }
}

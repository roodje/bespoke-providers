package com.yolt.providers.bunq.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestInquiry {

    @JsonProperty("amount_inquired")
    Amount amount = new Amount();

    @JsonProperty("counterparty_alias")
    Counterparty counterparty = new Counterparty();

    String description = "";

    @JsonProperty("allow_bunqme")
    boolean allowBunqMe = false;

    @Data
    class Amount {
        @JsonProperty("value")
        String value = "500.00";
        @JsonProperty("currency")
        String currency = "EUR";
    }

    @Data
    class Counterparty {
        @JsonProperty("type")
        String type = "EMAIL";
        @JsonProperty("value")
        String value = "sugardaddy@bunq.com";
        @JsonProperty("name")
        String name = "bunq sugardaddy";
    }
}

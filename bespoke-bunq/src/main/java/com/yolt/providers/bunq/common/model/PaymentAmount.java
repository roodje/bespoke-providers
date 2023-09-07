package com.yolt.providers.bunq.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

@Value
@Builder
@AllArgsConstructor
public class PaymentAmount {

    @JsonProperty("value")
    private String value;
    @JsonProperty("currency")
    private String currency;

    public void validate() {
        if (StringUtils.isEmpty(this.value) || StringUtils.isEmpty(this.currency)) {
            throw new IllegalArgumentException("Amount and currency are required");
        }
    }
}

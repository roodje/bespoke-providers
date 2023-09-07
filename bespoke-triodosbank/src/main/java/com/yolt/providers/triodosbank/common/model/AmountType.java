package com.yolt.providers.triodosbank.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AmountType {
    private String currency;
    private String amount;
}

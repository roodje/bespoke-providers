package com.yolt.providers.triodosbank.common.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Balance {

    private String balanceType;
    private AmountType balanceAmount;
    private String referenceDate;
    private Boolean creditLimitIncluded;
}

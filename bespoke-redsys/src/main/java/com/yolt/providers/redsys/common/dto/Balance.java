package com.yolt.providers.redsys.common.dto;

import lombok.Data;

@Data
public class Balance {
    private Amount balanceAmount;

    private String balanceType;

    private Boolean creditLimitIncluded;

    private String lastChangeDateTime;

    private String referenceDate;

    private String lastCommittedTransaction;
}

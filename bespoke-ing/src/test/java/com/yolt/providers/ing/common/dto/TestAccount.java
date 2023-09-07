package com.yolt.providers.ing.common.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TestAccount implements Accounts.Account {
    private String id;
    private String iban;
    private String maskedPan;
    private String name;
    private String currency;
    private String transactionLink;
    private String balancesLink;
    private String product;
}

package com.yolt.providers.redsys.common.dto;

import lombok.Data;

@Data
public class AccountReference {
    private String iban;

    private String bban;

    private String pan;

    private String maskedPan;

    private String msisdn;

    private String currency;
}

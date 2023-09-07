package com.yolt.providers.deutschebank.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public class DeutscheBankGroupProperties {

    @NotEmpty
    private String aisBaseUrl;

    @NotEmpty
    private String psuIdType;

    @Positive
    private int paginationLimit;
}

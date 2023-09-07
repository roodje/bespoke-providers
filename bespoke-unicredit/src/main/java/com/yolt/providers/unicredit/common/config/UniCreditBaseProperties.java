package com.yolt.providers.unicredit.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public abstract class UniCreditBaseProperties {
    @NotEmpty
    private String baseUrl;

    //Currently maximum accepted by the bank value is 4
    private int frequencyPerDay;

    private int paginationLimit;
}



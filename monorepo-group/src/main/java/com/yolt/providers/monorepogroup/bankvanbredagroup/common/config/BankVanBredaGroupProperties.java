package com.yolt.providers.monorepogroup.bankvanbredagroup.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Validated
public abstract class BankVanBredaGroupProperties {

    @NotEmpty
    private String baseUrl;
    @NotNull
    private int paginationLimit;
}
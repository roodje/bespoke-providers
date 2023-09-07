package com.yolt.providers.raiffeisenbank.common.ais.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Validated
public abstract class RaiffeisenBankProperties {

    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String oAuthBaseUrl;
    @NotNull
    private int paginationLimit;
}
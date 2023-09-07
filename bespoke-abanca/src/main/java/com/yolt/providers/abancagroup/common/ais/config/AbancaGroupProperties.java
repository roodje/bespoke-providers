package com.yolt.providers.abancagroup.common.ais.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Validated
public abstract class AbancaGroupProperties {

    @NotEmpty
    private String baseUrl;
    @NotNull
    private int paginationLimit;
}
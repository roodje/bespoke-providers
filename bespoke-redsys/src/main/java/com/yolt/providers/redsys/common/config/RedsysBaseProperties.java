package com.yolt.providers.redsys.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Validated
public abstract class RedsysBaseProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String authorizationUrl;

    @Min(1)
    @NotNull
    private int paginationLimit;
}

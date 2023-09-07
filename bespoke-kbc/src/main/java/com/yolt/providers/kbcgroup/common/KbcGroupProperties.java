package com.yolt.providers.kbcgroup.common;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;


@Data
@Validated
public abstract class KbcGroupProperties {

    @Min(1)
    private int paginationLimit;

    @NotNull
    private String authorizationUrl;

    @NotNull
    private String tokenUrl;

    @NotNull
    private String baseUrl;
}

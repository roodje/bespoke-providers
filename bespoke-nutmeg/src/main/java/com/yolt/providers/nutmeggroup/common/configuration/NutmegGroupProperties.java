package com.yolt.providers.nutmeggroup.common.configuration;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public abstract class NutmegGroupProperties {

    @NotEmpty
    private String authorizeUrl;

    @NotEmpty
    private String potsUrl;

    @NotEmpty
    private String tokenUrl;

    @NotEmpty
    private String scope;

    @NotEmpty
    private String audience;

    @NotEmpty
    private String codeChallengeMethod;
}
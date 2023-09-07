package com.yolt.providers.monorepogroup.libragroup.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public abstract class LibraGroupProperties {

    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String oAuthBaseUrl;
}
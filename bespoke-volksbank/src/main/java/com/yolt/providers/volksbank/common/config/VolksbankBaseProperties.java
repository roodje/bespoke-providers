package com.yolt.providers.volksbank.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public abstract class VolksbankBaseProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String authorizationUrl;

    private int paginationLimit;
}

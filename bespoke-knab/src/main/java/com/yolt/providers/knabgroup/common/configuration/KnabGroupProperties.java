package com.yolt.providers.knabgroup.common.configuration;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public abstract class KnabGroupProperties {

    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String authorizationUrl;
}
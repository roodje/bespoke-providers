package com.yolt.providers.monorepogroup.sebaggroup.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Validated
public class SebAGGroupProperties {

    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String tokenUrl;
    @NotEmpty
    private String authorizationUrl;
    @NotEmpty
    private String paginationLimit;
}


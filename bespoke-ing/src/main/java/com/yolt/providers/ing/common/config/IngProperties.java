package com.yolt.providers.ing.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@Validated
public abstract class IngProperties {

    @NotEmpty
    private String baseUrl;
    @NotEmpty
    private String oAuthTokenEndpoint;
    @NotEmpty
    private String authorizationUrlServerEndpoint;
    @NotNull
    private int paginationLimit;
    @NotNull
    private int fetchDataRetryLimit;
    private int transactionsPageSizeLimit;
}
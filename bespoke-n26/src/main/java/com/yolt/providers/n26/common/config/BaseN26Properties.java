package com.yolt.providers.n26.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public abstract class BaseN26Properties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String tokenEndpoint;

    @NotEmpty
    private String authorizationUrl;

    @Positive
    private int paginationLimit;

    @Positive
    private int corePoolSize = 2;

    private int consentStatusPollingTotalDelayLimitSeconds;

    private int consentStatusPollingInitialDelaySeconds;
}

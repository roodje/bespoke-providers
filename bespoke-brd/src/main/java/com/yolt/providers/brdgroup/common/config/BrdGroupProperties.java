package com.yolt.providers.brdgroup.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public class BrdGroupProperties {

    @NotEmpty
    private String baseUrl;

    @Positive
    private int paginationLimit;

    @Positive
    private int corePoolSize = 2;

    @Positive
    private int consentStatusPollingInitialDelayInSeconds;

    @Positive
    private int consentStatusPollingTotalDelayLimitInSeconds;
}

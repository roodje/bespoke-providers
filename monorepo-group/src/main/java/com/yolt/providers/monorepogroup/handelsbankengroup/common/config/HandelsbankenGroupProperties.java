package com.yolt.providers.monorepogroup.handelsbankengroup.common.config;

import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;

@Data
@Validated
public class HandelsbankenGroupProperties {

    @NotEmpty
    private String baseUrl;

    @Positive
    private int paginationLimit;

    @NotEmpty
    private String authorizationUrl;
}

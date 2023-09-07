package com.yolt.providers.amexgroup.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Validated
@Component
@ConfigurationProperties(prefix = "lovebird.amexgroup.amex")
public class AmexGroupConfigurationProperties {

    @Min(1)
    private int paginationLimit;

    @NotNull
    private String authorizationBaseUrlV2;

    @NotNull
    private String baseUrl;

    @NotNull
    private String host;

    @NotNull
    private String port;

    @NotNull
    private int pageSize;
}

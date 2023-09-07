package com.yolt.providers.rabobank.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;


@Data
@Component
@ConfigurationProperties("lovebird.rabobank")
@Validated
public class RabobankProperties {

    private final ResourceLoader resourceLoader;

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String baseAuthorizationUrl;

    @NotEmpty
    private String baseUrlPis;

    @NotEmpty
    private String oAuthAuthorizationScope;

    /**
     * Optional. Can be used to 'cap' the amount of transactions being returned. This was probably invented because
     * the sandbox returns 54600 transactions, which takes a while as well..
     */
    @Nullable
    @Positive
    private Integer transactionsFetchStartTimeMaxDays;

}
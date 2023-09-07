package com.yolt.providers.bunq.common.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotEmpty;

@Data
@Component
@ConfigurationProperties("lovebird.bunq")
@Validated
public class BunqProperties {

    @NotEmpty
    private String oauthTokenUrl;

    @NotEmpty
    private String oauthAuthorizationBaseUrl;

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String ourExternalIpAddress;

    private int accountsPerPage;

    private int transactionsPerPage;
}

package com.yolt.providers.starlingbank.common.configuration;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("lovebird.starlingbank")
public class StarlingBankProperties {

    @Value("${lovebird.starlingbank.baseUrl}")
    private String baseUrl;

    @Value("${lovebird.starlingbank.oAuthTokenUrl}")
    private String oAuthTokenUrl;

    @Value("${lovebird.starlingbank.oAuthAuthorizationBaseUrl}")
    private String oAuthAuthorizationBaseUrl;
}
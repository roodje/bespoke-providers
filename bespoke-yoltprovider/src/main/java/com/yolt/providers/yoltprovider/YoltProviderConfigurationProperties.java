package com.yolt.providers.yoltprovider;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("lovebird.yoltprovider")
public class YoltProviderConfigurationProperties {

    private String customerAuthorizationUrl;
    private String baseUrl;
}

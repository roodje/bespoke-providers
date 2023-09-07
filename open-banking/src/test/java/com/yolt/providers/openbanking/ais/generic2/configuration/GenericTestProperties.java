package com.yolt.providers.openbanking.ais.generic2.configuration;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("lovebird.generic2")
public class GenericTestProperties extends DefaultProperties {

    public static DefaultProperties generateTestProperties() {
        String baseUrl = "BaseUrl.configu.red";
        String oAuthAuthorizationUrl = "OAuthAuthorizationUrl.configu.red";
        String oAuthTokenUrl = "OAuthTokenUrl.configu.red";
        String https = "https://";
        GenericTestProperties properties = new GenericTestProperties();
        properties.setBaseUrl(https + baseUrl);
        properties.setOAuthAuthorizationUrl(https + oAuthAuthorizationUrl);
        properties.setOAuthTokenUrl(https + oAuthTokenUrl);
        return properties;
    }
}

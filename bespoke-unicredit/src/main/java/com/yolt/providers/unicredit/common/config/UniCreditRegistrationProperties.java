package com.yolt.providers.unicredit.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.unicredit.registration")
@Data
public class UniCreditRegistrationProperties {
    private String baseUrl;
}

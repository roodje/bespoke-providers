package com.yolt.providers.deutschebank.es;

import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.Positive;

@Component
@ConfigurationProperties("lovebird.deutschebankgroup.deutschebankes")
@Data
public class DeutscheBankEsProperties extends DeutscheBankGroupProperties {

    @Positive
    private int corePoolSize = 2;

    @Positive
    private int consentStatusPollingInitialDelayInSeconds;

    @Positive
    private int consentStatusPollingTotalDelayLimitInSeconds;
}

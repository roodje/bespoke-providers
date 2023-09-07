package com.yolt.providers.deutschebank.de;

import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.deutschebankgroup.deutschebank")
public class DeutscheBankProperties extends DeutscheBankGroupProperties {
}

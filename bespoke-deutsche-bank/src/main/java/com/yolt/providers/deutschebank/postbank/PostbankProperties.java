package com.yolt.providers.deutschebank.postbank;

import com.yolt.providers.deutschebank.common.config.DeutscheBankGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.deutschebankgroup.postbank")
public class PostbankProperties extends DeutscheBankGroupProperties {
}

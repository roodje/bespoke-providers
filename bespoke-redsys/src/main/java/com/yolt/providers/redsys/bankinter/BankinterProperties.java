package com.yolt.providers.redsys.bankinter;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.bankinter")
public class BankinterProperties extends RedsysBaseProperties {
}

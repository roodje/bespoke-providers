package com.yolt.providers.redsys.openbank;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.openbank")
public class OpenbankProperties extends RedsysBaseProperties {
}

package com.yolt.providers.redsys.bbva;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.bbva")
public class BBVAProperties extends RedsysBaseProperties {
}

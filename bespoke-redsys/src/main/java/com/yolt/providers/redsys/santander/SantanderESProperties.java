package com.yolt.providers.redsys.santander;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.santander")
public class SantanderESProperties extends RedsysBaseProperties {
}

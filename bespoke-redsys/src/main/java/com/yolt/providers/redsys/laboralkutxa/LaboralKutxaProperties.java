package com.yolt.providers.redsys.laboralkutxa;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.laboralkutxa")
public class LaboralKutxaProperties extends RedsysBaseProperties {
}

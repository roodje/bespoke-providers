package com.yolt.providers.redsys.sabadell;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.sabadell")
public class SabadellProperties extends RedsysBaseProperties {
}

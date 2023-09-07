package com.yolt.providers.redsys.cajarural;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.cajarural")
public class CajaRuralProperties extends RedsysBaseProperties {
}

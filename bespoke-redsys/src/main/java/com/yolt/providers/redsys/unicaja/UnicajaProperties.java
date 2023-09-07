package com.yolt.providers.redsys.unicaja;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.unicaja")
public class UnicajaProperties extends RedsysBaseProperties {
}

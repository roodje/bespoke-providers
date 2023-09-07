package com.yolt.providers.redsys.evo;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.evo")
public class EvoProperties extends RedsysBaseProperties {
}

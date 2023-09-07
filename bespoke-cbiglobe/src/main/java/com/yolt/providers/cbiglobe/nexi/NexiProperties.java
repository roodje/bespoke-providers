package com.yolt.providers.cbiglobe.nexi;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.cbiglobe.nexi")
public class NexiProperties extends CbiGlobeBaseProperties {
}

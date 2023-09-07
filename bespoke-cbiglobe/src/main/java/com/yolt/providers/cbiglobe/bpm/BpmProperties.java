package com.yolt.providers.cbiglobe.bpm;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.cbiglobe.bpm")
public class BpmProperties extends CbiGlobeBaseProperties {
}

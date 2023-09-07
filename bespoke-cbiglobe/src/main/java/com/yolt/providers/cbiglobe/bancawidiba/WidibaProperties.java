package com.yolt.providers.cbiglobe.bancawidiba;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.cbiglobe.bancawidiba")
public class WidibaProperties extends CbiGlobeBaseProperties {
}

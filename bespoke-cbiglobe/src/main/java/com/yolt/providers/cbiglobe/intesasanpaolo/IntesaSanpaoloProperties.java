package com.yolt.providers.cbiglobe.intesasanpaolo;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.cbiglobe.intesasanpaolo")
public class IntesaSanpaoloProperties extends CbiGlobeBaseProperties {
}

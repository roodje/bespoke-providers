package com.yolt.providers.cbiglobe.bnl;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.cbiglobe.bnl")
public class BnlProperties extends CbiGlobeBaseProperties {
}

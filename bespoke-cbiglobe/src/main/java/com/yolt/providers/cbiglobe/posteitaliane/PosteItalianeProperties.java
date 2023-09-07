package com.yolt.providers.cbiglobe.posteitaliane;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.cbiglobe.posteitaliane")
public class PosteItalianeProperties extends CbiGlobeBaseProperties {
}

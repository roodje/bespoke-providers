package com.yolt.providers.cbiglobe.montepaschisiena;

import com.yolt.providers.cbiglobe.common.config.CbiGlobeBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.cbiglobe.montepaschisiena")
public class MontePaschiSienaProperties extends CbiGlobeBaseProperties {
}

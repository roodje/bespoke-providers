package com.yolt.providers.redsys.cajamarcajarural;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.cajamarcajarural")
public class CajamarCajaRuralProperties extends RedsysBaseProperties {
}

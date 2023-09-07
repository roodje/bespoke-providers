package com.yolt.providers.redsys.cajasur;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.cajasur")
public class CajasurProperties extends RedsysBaseProperties {
}

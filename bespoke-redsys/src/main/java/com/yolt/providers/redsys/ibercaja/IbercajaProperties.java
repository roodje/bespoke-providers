package com.yolt.providers.redsys.ibercaja;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.ibercaja")
public class IbercajaProperties extends RedsysBaseProperties {
}

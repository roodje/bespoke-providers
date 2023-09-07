package com.yolt.providers.abancagroup.abanca.config;

import com.yolt.providers.abancagroup.common.ais.config.AbancaGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.abancagroup.abanca")
public class AbancaProperties extends AbancaGroupProperties {
}

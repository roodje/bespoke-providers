package com.yolt.providers.redsys.caixa;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.caixa")
public class CaixaProperties extends RedsysBaseProperties {
}

package com.yolt.providers.alpha.alphabankromania;

import com.yolt.providers.alpha.common.config.AlphaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.alpha.alphabankromania")
public class AlphaBankRomaniaProperties extends AlphaProperties {
}
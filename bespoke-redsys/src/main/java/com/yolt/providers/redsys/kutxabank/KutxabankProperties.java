package com.yolt.providers.redsys.kutxabank;

import com.yolt.providers.redsys.common.config.RedsysBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.redsys.kutxabank")
public class KutxabankProperties extends RedsysBaseProperties {
}

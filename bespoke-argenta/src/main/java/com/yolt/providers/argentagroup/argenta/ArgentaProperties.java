package com.yolt.providers.argentagroup.argenta;

import com.yolt.providers.argentagroup.common.CommonProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.argentagroup.argenta")
public class ArgentaProperties extends CommonProperties {

}

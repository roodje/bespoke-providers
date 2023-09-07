package com.yolt.providers.gruppocedacri.bancamediolanum.config;

import com.yolt.providers.gruppocedacri.common.config.GruppoCedacriProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.gruppocedacri.bancamediolanum")
public class BancaMediolanumProperties extends GruppoCedacriProperties {

}

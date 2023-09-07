package com.yolt.providers.monorepogroup.olbgroup.olb;

import com.yolt.providers.monorepogroup.olbgroup.common.config.OlbGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.olbgroup.olb")
public class OlbProperties extends OlbGroupProperties {

}

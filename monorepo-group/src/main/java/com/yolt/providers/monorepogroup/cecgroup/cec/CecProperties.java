package com.yolt.providers.monorepogroup.cecgroup.cec;

import com.yolt.providers.monorepogroup.cecgroup.common.config.CecGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.cecgroup.cec")
public class CecProperties extends CecGroupProperties {

}
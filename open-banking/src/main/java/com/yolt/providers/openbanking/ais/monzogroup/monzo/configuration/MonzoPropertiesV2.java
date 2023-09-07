package com.yolt.providers.openbanking.ais.monzogroup.monzo.configuration;

import com.yolt.providers.openbanking.ais.monzogroup.common.MonzoGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.monzogroup.monzo")
public class MonzoPropertiesV2 extends MonzoGroupPropertiesV2 {

}

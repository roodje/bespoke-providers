package com.yolt.providers.openbanking.ais.rbsgroup.coutts;

import com.yolt.providers.openbanking.ais.rbsgroup.common.properties.RbsGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.rbsgroup.coutts")
public class CouttsPropertiesV2 extends RbsGroupPropertiesV2 {
}

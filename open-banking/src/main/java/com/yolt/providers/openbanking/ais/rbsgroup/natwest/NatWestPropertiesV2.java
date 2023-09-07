package com.yolt.providers.openbanking.ais.rbsgroup.natwest;

import com.yolt.providers.openbanking.ais.rbsgroup.common.properties.RbsGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.rbsgroup.natwest")
public class NatWestPropertiesV2 extends RbsGroupPropertiesV2 {
}

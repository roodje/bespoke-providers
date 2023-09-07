package com.yolt.providers.openbanking.ais.rbsgroup.natwestcorporate;

import com.yolt.providers.openbanking.ais.rbsgroup.common.properties.RbsGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.rbsgroup.natwest-corporate")
public class NatWestCorporatePropertiesV2 extends RbsGroupPropertiesV2 {
}

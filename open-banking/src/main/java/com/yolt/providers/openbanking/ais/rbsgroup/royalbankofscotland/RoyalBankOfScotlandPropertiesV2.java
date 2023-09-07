package com.yolt.providers.openbanking.ais.rbsgroup.royalbankofscotland;

import com.yolt.providers.openbanking.ais.rbsgroup.common.properties.RbsGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.rbsgroup.rbs")
public class RoyalBankOfScotlandPropertiesV2 extends RbsGroupPropertiesV2 {
}

package com.yolt.providers.openbanking.ais.rbsgroup.royalbankofscotlandcorporate;

import com.yolt.providers.openbanking.ais.rbsgroup.common.properties.RbsGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.rbsgroup.rbs-corporate")
public class RoyalBankOfScotlandCorporatePropertiesV2 extends RbsGroupPropertiesV2 {
}

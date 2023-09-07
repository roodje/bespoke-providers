package com.yolt.providers.openbanking.ais.lloydsbankinggroup.lloydscorporate;

import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.config.LloydsBankingGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.lloydsgroup.lloyds-corporate")
public class LloydsBankCorpoPropertiesV2 extends LloydsBankingGroupPropertiesV2 {

}

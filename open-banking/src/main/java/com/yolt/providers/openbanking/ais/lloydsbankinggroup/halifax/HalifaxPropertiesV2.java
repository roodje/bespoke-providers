package com.yolt.providers.openbanking.ais.lloydsbankinggroup.halifax;

import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.config.LloydsBankingGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.lloydsgroup.halifax")
public class HalifaxPropertiesV2 extends LloydsBankingGroupPropertiesV2 {

}

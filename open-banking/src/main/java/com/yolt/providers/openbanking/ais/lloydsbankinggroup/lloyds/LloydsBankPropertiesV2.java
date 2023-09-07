package com.yolt.providers.openbanking.ais.lloydsbankinggroup.lloyds;

import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.config.LloydsBankingGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.lloydsgroup.lloyds")
public class LloydsBankPropertiesV2 extends LloydsBankingGroupPropertiesV2 {

}

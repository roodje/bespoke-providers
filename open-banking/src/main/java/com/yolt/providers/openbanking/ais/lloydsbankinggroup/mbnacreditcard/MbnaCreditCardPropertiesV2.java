package com.yolt.providers.openbanking.ais.lloydsbankinggroup.mbnacreditcard;

import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.config.LloydsBankingGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.lloydsgroup.mbna-credit-card")
public class MbnaCreditCardPropertiesV2 extends LloydsBankingGroupPropertiesV2 {

}

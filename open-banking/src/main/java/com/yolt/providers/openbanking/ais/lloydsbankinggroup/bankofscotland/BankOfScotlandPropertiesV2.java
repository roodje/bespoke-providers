package com.yolt.providers.openbanking.ais.lloydsbankinggroup.bankofscotland;

import com.yolt.providers.openbanking.ais.lloydsbankinggroup.common.config.LloydsBankingGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.lloydsgroup.bankofscotland")
public class BankOfScotlandPropertiesV2 extends LloydsBankingGroupPropertiesV2 {

}

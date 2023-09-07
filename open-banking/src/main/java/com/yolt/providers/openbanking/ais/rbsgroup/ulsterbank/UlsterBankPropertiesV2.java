package com.yolt.providers.openbanking.ais.rbsgroup.ulsterbank;

import com.yolt.providers.openbanking.ais.rbsgroup.common.properties.RbsGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.rbsgroup.ulsterbank")
public class UlsterBankPropertiesV2 extends RbsGroupPropertiesV2 {
}

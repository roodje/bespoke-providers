package com.yolt.providers.openbanking.ais.aibgroup.aibie;

import com.yolt.providers.openbanking.ais.aibgroup.common.AibGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.aibgroup.aib-ie")
public class AibIePropertiesV1 extends AibGroupPropertiesV2 {

}

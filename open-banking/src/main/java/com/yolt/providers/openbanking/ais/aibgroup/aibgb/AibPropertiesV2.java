package com.yolt.providers.openbanking.ais.aibgroup.aibgb;

import com.yolt.providers.openbanking.ais.aibgroup.common.AibGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.aibgroup.aib")
public class AibPropertiesV2 extends AibGroupPropertiesV2 {

}

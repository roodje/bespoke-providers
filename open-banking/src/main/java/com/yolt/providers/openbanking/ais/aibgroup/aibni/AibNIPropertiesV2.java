package com.yolt.providers.openbanking.ais.aibgroup.aibni;

import com.yolt.providers.openbanking.ais.aibgroup.common.AibGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("lovebird.aibgroup.aib-ni")
public class AibNIPropertiesV2 extends AibGroupPropertiesV2 {

}
package com.yolt.providers.openbanking.ais.tidegroup.tide;

import com.yolt.providers.openbanking.ais.tidegroup.common.TideGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.tidegroup.tide")
public class TidePropertiesV2 extends TideGroupPropertiesV2 {
}




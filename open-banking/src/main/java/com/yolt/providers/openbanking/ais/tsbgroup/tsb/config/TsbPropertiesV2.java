package com.yolt.providers.openbanking.ais.tsbgroup.tsb.config;

import com.yolt.providers.openbanking.ais.tsbgroup.common.config.TsbGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.tsbgroup.tsb")

public class TsbPropertiesV2 extends TsbGroupPropertiesV2 {

}

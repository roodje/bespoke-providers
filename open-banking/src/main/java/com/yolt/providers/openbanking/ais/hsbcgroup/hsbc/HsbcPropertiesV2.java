package com.yolt.providers.openbanking.ais.hsbcgroup.hsbc;

import com.yolt.providers.openbanking.ais.hsbcgroup.common.properties.HsbcGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.hsbcgroup.hsbc")
public class HsbcPropertiesV2 extends HsbcGroupPropertiesV2 {

}

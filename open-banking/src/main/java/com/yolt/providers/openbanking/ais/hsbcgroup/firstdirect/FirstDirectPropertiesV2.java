package com.yolt.providers.openbanking.ais.hsbcgroup.firstdirect;

import com.yolt.providers.openbanking.ais.hsbcgroup.common.properties.HsbcGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.hsbcgroup.firstdirect")
public class FirstDirectPropertiesV2 extends HsbcGroupPropertiesV2 {

}
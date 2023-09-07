package com.yolt.providers.openbanking.ais.newdaygroup.argos;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.newdaygroup.argos")
public class ArgosPropertiesV2 extends DefaultProperties {

}

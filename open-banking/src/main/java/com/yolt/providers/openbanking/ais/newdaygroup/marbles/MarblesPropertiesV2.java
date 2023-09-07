package com.yolt.providers.openbanking.ais.newdaygroup.marbles;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.newdaygroup.marbles")
public class MarblesPropertiesV2 extends DefaultProperties {

}
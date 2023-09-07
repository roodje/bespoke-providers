package com.yolt.providers.openbanking.ais.kbciegroup.kbcie;

import com.yolt.providers.openbanking.ais.kbciegroup.common.properties.KbcIeGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.kbciegroup.kbcie")
public class KbcIeProperties extends KbcIeGroupProperties {

}

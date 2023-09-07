package com.yolt.providers.openbanking.ais.hsbcgroup.marksandspencer;

import com.yolt.providers.openbanking.ais.hsbcgroup.common.properties.HsbcGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.hsbcgroup.marksandspencer")
public class MarksAndSpencerPropertiesV2 extends HsbcGroupPropertiesV2 {

}

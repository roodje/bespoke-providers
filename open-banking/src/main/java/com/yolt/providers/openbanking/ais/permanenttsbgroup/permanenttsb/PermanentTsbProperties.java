package com.yolt.providers.openbanking.ais.permanenttsbgroup.permanenttsb;

import com.yolt.providers.openbanking.ais.permanenttsbgroup.common.properties.PermanentTsbGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.permanenttsbgroup.permanenttsb")
public class PermanentTsbProperties extends PermanentTsbGroupProperties {

}

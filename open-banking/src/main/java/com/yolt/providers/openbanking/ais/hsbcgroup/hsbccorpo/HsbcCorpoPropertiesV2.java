package com.yolt.providers.openbanking.ais.hsbcgroup.hsbccorpo;

import com.yolt.providers.openbanking.ais.hsbcgroup.common.properties.HsbcGroupPropertiesV2;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("lovebird.hsbcgroup.hsbc-corporate")
public class HsbcCorpoPropertiesV2 extends HsbcGroupPropertiesV2 {

}

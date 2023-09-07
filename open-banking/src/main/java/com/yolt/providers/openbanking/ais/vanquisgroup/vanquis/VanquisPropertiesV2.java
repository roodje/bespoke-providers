package com.yolt.providers.openbanking.ais.vanquisgroup.vanquis;

import com.yolt.providers.openbanking.ais.vanquisgroup.common.properties.VanquisGroupPropertiesV2;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties("lovebird.vanquisgroup.vanquis")
public class VanquisPropertiesV2 extends VanquisGroupPropertiesV2 {

}
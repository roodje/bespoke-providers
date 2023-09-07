package com.yolt.providers.openbanking.ais.newdaygroup.amazoncreditcard;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties("lovebird.newdaygroup.amazoncreditcard")
public class AmazonCreditCardPropertiesV2 extends DefaultProperties {

}

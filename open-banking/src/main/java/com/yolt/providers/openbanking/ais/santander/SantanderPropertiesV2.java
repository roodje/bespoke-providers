package com.yolt.providers.openbanking.ais.santander;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Component
@Validated
@EqualsAndHashCode(callSuper = true)
@ConfigurationProperties("lovebird.santander")
public class SantanderPropertiesV2 extends DefaultProperties {

}
package com.yolt.providers.stet.cmarkeagroup.abp.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("ArkeaBanquePriveeStetProperties")
@ConfigurationProperties("lovebird.stet.cmarkeagroup.arkeabanqueprivee")
@EqualsAndHashCode(callSuper = true)
public class ArkeaBanquePriveeProperties extends DefaultProperties {
}

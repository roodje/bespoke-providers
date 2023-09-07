package com.yolt.providers.stet.cmarkeagroup.fortuneo.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("FortuneoStetProperties")
@ConfigurationProperties("lovebird.stet.cmarkeagroup.fortuneo")
@EqualsAndHashCode(callSuper = true)
public class FortuneoProperties extends DefaultProperties {
}

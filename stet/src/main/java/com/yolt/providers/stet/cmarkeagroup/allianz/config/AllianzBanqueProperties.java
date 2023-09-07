package com.yolt.providers.stet.cmarkeagroup.allianz.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("AllianzBanqueStetProperties")
@ConfigurationProperties("lovebird.stet.cmarkeagroup.allianzbanque")
@EqualsAndHashCode(callSuper = true)
public class AllianzBanqueProperties extends DefaultProperties {
}

package com.yolt.providers.stet.cicgroup.cic.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("CicStetProperties")
@ConfigurationProperties("lovebird.stet.cicgroup.cic")
@EqualsAndHashCode(callSuper = true)
public class CicProperties extends DefaultProperties {
}

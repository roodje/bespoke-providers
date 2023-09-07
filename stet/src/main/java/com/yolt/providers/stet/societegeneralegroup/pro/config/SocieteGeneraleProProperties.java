package com.yolt.providers.stet.societegeneralegroup.pro.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("SocieteGeneraleProStetProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.pro")
@EqualsAndHashCode(callSuper = true)
public class SocieteGeneraleProProperties extends DefaultProperties {
}

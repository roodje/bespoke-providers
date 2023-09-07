package com.yolt.providers.stet.societegeneralegroup.pri.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("SocieteGeneralePriStetProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.pri")
@EqualsAndHashCode(callSuper = true)
public class SocieteGeneralePriProperties extends DefaultProperties {
}

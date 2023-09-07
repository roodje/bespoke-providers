package com.yolt.providers.stet.societegeneralegroup.ent.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("SocieteGeneraleEntStetProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.ent")
@EqualsAndHashCode(callSuper = true)
public class SocieteGeneraleEntProperties extends DefaultProperties {
}

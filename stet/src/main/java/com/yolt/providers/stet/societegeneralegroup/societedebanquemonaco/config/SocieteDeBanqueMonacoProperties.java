package com.yolt.providers.stet.societegeneralegroup.societedebanquemonaco.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("SocieteDeBanqueMonacoProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.societedebanquemonaco")
@EqualsAndHashCode(callSuper = true)
public class SocieteDeBanqueMonacoProperties extends DefaultProperties {
}

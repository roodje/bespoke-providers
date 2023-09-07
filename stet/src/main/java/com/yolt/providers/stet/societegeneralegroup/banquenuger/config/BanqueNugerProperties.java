package com.yolt.providers.stet.societegeneralegroup.banquenuger.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("BanqueNugerProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.banquenuger")
@EqualsAndHashCode(callSuper = true)
public class BanqueNugerProperties extends DefaultProperties {
}

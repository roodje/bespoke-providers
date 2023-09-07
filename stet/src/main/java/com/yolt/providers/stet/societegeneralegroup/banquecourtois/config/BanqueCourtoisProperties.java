package com.yolt.providers.stet.societegeneralegroup.banquecourtois.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("BanqueCourtoisProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.banquecourtois")
@EqualsAndHashCode(callSuper = true)
public class BanqueCourtoisProperties extends DefaultProperties {
}

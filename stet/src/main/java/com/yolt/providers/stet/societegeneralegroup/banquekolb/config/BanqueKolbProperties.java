package com.yolt.providers.stet.societegeneralegroup.banquekolb.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("BanqueKolbProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.banquekolb")
@EqualsAndHashCode(callSuper = true)
public class BanqueKolbProperties extends DefaultProperties {
}

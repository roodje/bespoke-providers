package com.yolt.providers.stet.societegeneralegroup.banquerhonealpes.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("BanqueRhoneAlpesProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.banquerhonealpes")
@EqualsAndHashCode(callSuper = true)
public class BanqueRhoneAlpesProperties extends DefaultProperties {
}

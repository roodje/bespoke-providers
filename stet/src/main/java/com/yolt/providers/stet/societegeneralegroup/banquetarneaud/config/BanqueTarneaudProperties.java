package com.yolt.providers.stet.societegeneralegroup.banquetarneaud.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("BanqueTarneaudProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.banquetarneaud")
@EqualsAndHashCode(callSuper = true)
public class BanqueTarneaudProperties extends DefaultProperties {
}

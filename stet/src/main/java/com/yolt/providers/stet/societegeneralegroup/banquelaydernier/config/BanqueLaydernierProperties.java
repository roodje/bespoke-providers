package com.yolt.providers.stet.societegeneralegroup.banquelaydernier.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("BanqueLaydernierProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.banquelaydernier")
@EqualsAndHashCode(callSuper = true)
public class BanqueLaydernierProperties extends DefaultProperties {
}

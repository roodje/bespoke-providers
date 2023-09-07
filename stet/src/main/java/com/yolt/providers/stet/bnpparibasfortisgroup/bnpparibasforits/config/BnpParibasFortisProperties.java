package com.yolt.providers.stet.bnpparibasfortisgroup.bnpparibasforits.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component("BnpParibasFortisStetProperties")
@ConfigurationProperties("lovebird.stet.bnpparibasfortisgroup.bnpparibasfortis")
@EqualsAndHashCode(callSuper = true)
public class BnpParibasFortisProperties extends DefaultProperties {
}

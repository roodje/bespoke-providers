package com.yolt.providers.stet.bnpparibasgroup.bnpparibas.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
@Validated
@Component("BnpParibasStetProperties")
@ConfigurationProperties("lovebird.stet.bnpparibasgroup.bnpparibas")
@EqualsAndHashCode(callSuper = true)

public class BnpParibasProperties extends DefaultProperties {
}

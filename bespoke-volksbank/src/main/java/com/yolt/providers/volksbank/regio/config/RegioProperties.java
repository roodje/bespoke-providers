package com.yolt.providers.volksbank.regio.config;

import com.yolt.providers.volksbank.common.config.VolksbankBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.volksbank.regio")
public class RegioProperties extends VolksbankBaseProperties {
}

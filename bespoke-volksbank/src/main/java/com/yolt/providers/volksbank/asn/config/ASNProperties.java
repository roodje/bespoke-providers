package com.yolt.providers.volksbank.asn.config;

import com.yolt.providers.volksbank.common.config.VolksbankBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.volksbank.asn")
public class ASNProperties extends VolksbankBaseProperties {
}

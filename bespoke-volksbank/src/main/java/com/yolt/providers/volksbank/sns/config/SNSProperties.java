package com.yolt.providers.volksbank.sns.config;

import com.yolt.providers.volksbank.common.config.VolksbankBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.volksbank.sns")
public class SNSProperties extends VolksbankBaseProperties {
}

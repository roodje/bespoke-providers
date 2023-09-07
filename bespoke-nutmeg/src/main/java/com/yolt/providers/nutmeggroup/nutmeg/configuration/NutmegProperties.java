package com.yolt.providers.nutmeggroup.nutmeg.configuration;

import com.yolt.providers.nutmeggroup.common.configuration.NutmegGroupProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Qualifier("NutmegProperties")
@ConfigurationProperties("lovebird.nutmeggroup.nutmeg")
public class NutmegProperties extends NutmegGroupProperties {
}
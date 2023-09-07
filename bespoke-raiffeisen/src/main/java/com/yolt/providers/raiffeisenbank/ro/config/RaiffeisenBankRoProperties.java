package com.yolt.providers.raiffeisenbank.ro.config;

import com.yolt.providers.raiffeisenbank.common.ais.config.RaiffeisenBankProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.raiffeisenbank.ro")
public class RaiffeisenBankRoProperties extends RaiffeisenBankProperties {
}

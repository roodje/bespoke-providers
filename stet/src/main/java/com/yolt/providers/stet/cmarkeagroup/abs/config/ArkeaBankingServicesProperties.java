package com.yolt.providers.stet.cmarkeagroup.abs.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("ArkeaBankingServicesStetProperties")
@ConfigurationProperties("lovebird.stet.cmarkeagroup.arkeabankingservices")
@EqualsAndHashCode(callSuper = true)
public class ArkeaBankingServicesProperties extends DefaultProperties {
}

package com.yolt.providers.stet.cmarkeagroup.max.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("MaxBankStetProperties")
@ConfigurationProperties("lovebird.stet.cmarkeagroup.maxbank")
@EqualsAndHashCode(callSuper = true)
public class MaxBankProperties extends DefaultProperties {
}

package com.yolt.providers.stet.cmarkeagroup.axa.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("AxaStetProperties")
@ConfigurationProperties("lovebird.stet.cmarkeagroup.axa")
@EqualsAndHashCode(callSuper = true)
public class AxaProperties extends DefaultProperties {
}

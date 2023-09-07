package com.yolt.providers.stet.cicgroup.beobank.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component
@ConfigurationProperties("lovebird.stet.cicgroup.beobank")
@EqualsAndHashCode(callSuper = true)
public class BeobankProperties extends DefaultProperties {
}

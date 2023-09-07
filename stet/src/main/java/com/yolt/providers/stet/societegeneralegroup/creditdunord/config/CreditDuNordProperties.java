package com.yolt.providers.stet.societegeneralegroup.creditdunord.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("CreditDuNordProperties")
@ConfigurationProperties("lovebird.stet.societegeneralegroup.creditdunord")
@EqualsAndHashCode(callSuper = true)
public class CreditDuNordProperties extends DefaultProperties {
}

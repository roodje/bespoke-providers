package com.yolt.providers.stet.boursoramagroup.boursorama.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("BoursoramaStetProperties")
@ConfigurationProperties("lovebird.stet.boursoramagroup.boursorama")
@EqualsAndHashCode(callSuper = true)
public class BoursoramaProperties extends DefaultProperties {
}

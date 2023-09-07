package com.yolt.providers.stet.cmarkeagroup.bpe.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("BpeStetProperties")
@ConfigurationProperties("lovebird.stet.cmarkeagroup.bpe")
@EqualsAndHashCode(callSuper = true)
public class BpeProperties extends DefaultProperties {
}

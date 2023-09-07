package com.yolt.providers.stet.labanquepostalegroup.labanquepostale.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("LaBanquePostaleStetProperties")
@ConfigurationProperties("lovebird.stet.labanquepostalegroup.labanquepostale")
@EqualsAndHashCode(callSuper = true)
public class LaBanquePostaleProperties extends DefaultProperties {
}

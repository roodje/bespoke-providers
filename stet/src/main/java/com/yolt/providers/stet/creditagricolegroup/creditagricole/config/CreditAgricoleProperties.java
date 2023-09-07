package com.yolt.providers.stet.creditagricolegroup.creditagricole.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("CreditAgricoleStetProperties")
@ConfigurationProperties("lovebird.stet.creditagricolegroup.creditagricole")
@EqualsAndHashCode(callSuper = true)
public class CreditAgricoleProperties extends DefaultProperties {
}

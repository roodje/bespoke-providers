package com.yolt.providers.stet.cicgroup.creditmutuel.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("CreditMutuelStetProperties")
@ConfigurationProperties("lovebird.stet.cicgroup.creditmutuel")
@EqualsAndHashCode(callSuper = true)
public class CreditMutuelProperties extends DefaultProperties {
}

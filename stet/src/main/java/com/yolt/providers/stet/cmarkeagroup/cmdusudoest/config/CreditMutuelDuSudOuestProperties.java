package com.yolt.providers.stet.cmarkeagroup.cmdusudoest.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("CreditMutuelDuSudOuestStetProperties")
@ConfigurationProperties("lovebird.stet.cmarkeagroup.creditmutueldusudouest")
@EqualsAndHashCode(callSuper = true)
public class CreditMutuelDuSudOuestProperties extends DefaultProperties {
}

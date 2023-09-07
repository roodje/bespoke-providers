package com.yolt.providers.stet.bpcegroup.caissedepargneparticuliers.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("CaisseDepargneParticuliersStetProperties")
@ConfigurationProperties("lovebird.stet.bpcegroup.caissedepargneparticuliers")
@EqualsAndHashCode(callSuper = true)
public class CaisseDepargneParticuliersProperties extends DefaultProperties {
}

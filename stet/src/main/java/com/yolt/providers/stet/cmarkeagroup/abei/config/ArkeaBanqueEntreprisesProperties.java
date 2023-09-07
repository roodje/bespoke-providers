package com.yolt.providers.stet.cmarkeagroup.abei.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("ArkeaBanqueEntreprisesStetProperties")
@ConfigurationProperties("lovebird.stet.cmarkeagroup.arkeabanqueentreprises")
@EqualsAndHashCode(callSuper = true)
public class ArkeaBanqueEntreprisesProperties extends DefaultProperties {
}

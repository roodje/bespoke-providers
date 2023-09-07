package com.yolt.providers.stet.bpcegroup.banquepopulaire.config;

import com.yolt.providers.stet.generic.config.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Component("BanquePopulaireStetProperties")
@ConfigurationProperties("lovebird.stet.bpcegroup.banquepopulaire")
@EqualsAndHashCode(callSuper = true)
public class BanquePopulaireProperties extends DefaultProperties {
}

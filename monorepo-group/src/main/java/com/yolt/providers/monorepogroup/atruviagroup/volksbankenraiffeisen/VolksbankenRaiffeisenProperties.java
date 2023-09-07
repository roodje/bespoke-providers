package com.yolt.providers.monorepogroup.atruviagroup.volksbankenraiffeisen;

import com.yolt.providers.monorepogroup.atruviagroup.common.AtruviaGroupProperties;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties("lovebird.atruviagroup.volksbankenraiffeisen")
@EqualsAndHashCode(callSuper = true)
public class VolksbankenRaiffeisenProperties extends AtruviaGroupProperties {
}

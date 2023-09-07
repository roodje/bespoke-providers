package com.yolt.providers.monorepogroup.chebancagroup;

import com.yolt.providers.monorepogroup.chebancagroup.common.CheBancaGroupProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.chebancagroup.chebanca")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CheBancaProperties extends CheBancaGroupProperties {
}

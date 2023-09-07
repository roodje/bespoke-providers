package com.yolt.providers.monorepogroup.raiffeisenatgroup.raiffeisenat;

import com.yolt.providers.monorepogroup.raiffeisenatgroup.common.config.RaiffeisenAtGroupProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.raiffeisenatgroup.raiffeisenat")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class RaiffeisenAtProperties extends RaiffeisenAtGroupProperties {
}

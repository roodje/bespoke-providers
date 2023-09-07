package com.yolt.providers.monorepogroup.qontogroup.qonto;

import com.yolt.providers.monorepogroup.qontogroup.common.config.QontoGroupProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.qontogroup.qonto")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class QontoProperties extends QontoGroupProperties {
}

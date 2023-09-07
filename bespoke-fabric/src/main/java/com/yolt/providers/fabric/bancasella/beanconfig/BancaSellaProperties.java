package com.yolt.providers.fabric.bancasella.beanconfig;

import com.yolt.providers.fabric.common.beanconfig.FabricGroupProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("lovebird.fabricgroup.bancasella")
public class BancaSellaProperties extends FabricGroupProperties {
}

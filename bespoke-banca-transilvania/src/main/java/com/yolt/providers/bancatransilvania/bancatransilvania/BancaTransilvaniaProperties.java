package com.yolt.providers.bancatransilvania.bancatransilvania;

import com.yolt.providers.bancatransilvania.common.config.BancaTransilvaniaGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.bancatransilvaniagroup.bancatransilvania")
public class BancaTransilvaniaProperties extends BancaTransilvaniaGroupProperties {
}

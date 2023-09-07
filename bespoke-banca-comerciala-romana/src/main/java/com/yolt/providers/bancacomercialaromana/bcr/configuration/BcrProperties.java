package com.yolt.providers.bancacomercialaromana.bcr.configuration;

import com.yolt.providers.bancacomercialaromana.common.configuration.BcrGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.bcrgroup.bcr")
public class BcrProperties extends BcrGroupProperties {
}

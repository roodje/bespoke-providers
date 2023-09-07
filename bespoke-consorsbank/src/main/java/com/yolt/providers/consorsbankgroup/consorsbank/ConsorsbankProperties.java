package com.yolt.providers.consorsbankgroup.consorsbank;

import com.yolt.providers.consorsbankgroup.common.ais.DefaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.consorsbankgroup.consorsbank")
public class ConsorsbankProperties extends DefaultProperties {
}

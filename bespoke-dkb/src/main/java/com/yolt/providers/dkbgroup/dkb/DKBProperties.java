package com.yolt.providers.dkbgroup.dkb;

import com.yolt.providers.dkbgroup.common.DKBGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.dkbgroup.dkb")
public class DKBProperties extends DKBGroupProperties {
}

package com.yolt.providers.monorepogroup.libragroup.libra;

import com.yolt.providers.monorepogroup.libragroup.common.config.LibraGroupProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.libragroup.libra")
public class LibraProperties extends LibraGroupProperties {

}
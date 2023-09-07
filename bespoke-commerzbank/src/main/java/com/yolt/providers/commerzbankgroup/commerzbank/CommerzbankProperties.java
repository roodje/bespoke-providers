package com.yolt.providers.commerzbankgroup.commerzbank;

import com.yolt.providers.commerzbankgroup.common.CommerzbankBaseProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.commerzbankgroup.commerzbank")
class CommerzbankProperties extends CommerzbankBaseProperties {
}

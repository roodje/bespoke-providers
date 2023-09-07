package com.yolt.providers.openbanking.ais.amexgroup.amex;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("lovebird.amexgroup.amexob")
public class AmexProperties extends DefaultProperties {
}

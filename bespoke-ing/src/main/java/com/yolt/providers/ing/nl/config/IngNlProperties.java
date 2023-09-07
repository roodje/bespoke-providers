package com.yolt.providers.ing.nl.config;

import com.yolt.providers.ing.common.config.IngProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
@ConfigurationProperties("lovebird.ing.nl")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class IngNlProperties extends IngProperties {

    public static final String PROVIDER_IDENTIFIER = "ING_NL";
    public static final String PROVIDER_IDENTIFIER_DISPLAY_NAME = "ING NL";

    static final ZoneId ZONE_ID = ZoneId.of("Europe/Amsterdam");
    static final String COUNTRY_CODE = "NL";

    public IngNlProperties() {
        super();
    }
}
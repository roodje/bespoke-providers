package com.yolt.providers.ing.it.config;

import com.yolt.providers.ing.common.config.IngProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
@ConfigurationProperties("lovebird.ing.it")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class IngItProperties extends IngProperties {

    public static final String PROVIDER_IDENTIFIER = "ING_IT";
    public static final String PROVIDER_IDENTIFIER_DISPLAY_NAME = "ING IT";

    static final ZoneId ZONE_ID = ZoneId.of("Europe/Rome");
    static final String COUNTRY_CODE = "IT";

    public IngItProperties() {
        super();
    }
}
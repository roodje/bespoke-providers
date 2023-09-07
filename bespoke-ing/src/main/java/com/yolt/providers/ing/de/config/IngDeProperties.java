package com.yolt.providers.ing.de.config;

import com.yolt.providers.ing.common.config.IngProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
@ConfigurationProperties("lovebird.ing.de")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class IngDeProperties extends IngProperties {

    public static final String PROVIDER_IDENTIFIER = "ING_DE";
    public static final String PROVIDER_IDENTIFIER_DISPLAY_NAME = "ING DE";

    static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
    static final String COUNTRY_CODE = "DE";

    public IngDeProperties() {
        super();
    }

}
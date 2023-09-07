package com.yolt.providers.ing.be.config;

import com.yolt.providers.ing.common.config.IngProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
@ConfigurationProperties("lovebird.ing.be")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class IngBeProperties extends IngProperties {

    public static final String PROVIDER_IDENTIFIER = "ING_BE";
    public static final String PROVIDER_IDENTIFIER_DISPLAY_NAME = "ING BE";

    static final ZoneId ZONE_ID = ZoneId.of("Europe/Brussels");
    static final String COUNTRY_CODE = "BE";

    public IngBeProperties() {
        super();
    }

}
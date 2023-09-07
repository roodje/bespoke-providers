package com.yolt.providers.ing.fr.config;

import com.yolt.providers.ing.common.config.IngProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
@ConfigurationProperties("lovebird.ing.fr")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class IngFrProperties extends IngProperties {

    public static final String PROVIDER_IDENTIFIER = "ING_FR";
    public static final String PROVIDER_IDENTIFIER_DISPLAY_NAME = "ING FR";

    static final ZoneId ZONE_ID = ZoneId.of("Europe/Paris");
    static final String COUNTRY_CODE = "FR";

    public IngFrProperties() {
        super();
    }
}
package com.yolt.providers.ing.ro.config;

import com.yolt.providers.ing.common.config.IngProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.ZoneId;

@Component
@ConfigurationProperties("lovebird.ing.ro")
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class IngRoProperties extends IngProperties {

    public static final String PROVIDER_IDENTIFIER = "ING_RO";
    public static final String PROVIDER_IDENTIFIER_DISPLAY_NAME = "ING RO";

    static final ZoneId ZONE_ID = ZoneId.of("Europe/Bucharest");
    static final String COUNTRY_CODE = "RO";

    public IngRoProperties() {
        super();
    }

}
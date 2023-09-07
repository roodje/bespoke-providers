package com.yolt.providers.openbanking.ais.danske.beanconfig;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@Component
@ConfigurationProperties("lovebird.danske")
@EqualsAndHashCode(callSuper = true)
@Data
public class DanskeBankPropertiesV4 extends DefaultProperties {
    public static final String PROVIDER_IDENTIFIER = "DANSKEBANK";
    public static final String PROVIDER_DISPLAY_NAME = "Danske Bank";

    @NotNull
    protected String registrationUrl;
}


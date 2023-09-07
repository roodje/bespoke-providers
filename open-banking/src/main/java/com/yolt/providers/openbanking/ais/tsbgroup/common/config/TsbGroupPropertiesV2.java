package com.yolt.providers.openbanking.ais.tsbgroup.common.config;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode
@Validated
public class TsbGroupPropertiesV2 extends DefaultProperties {

    @Setter
    @Getter
    @Min(value = 0, message = "Should be positive value")
    private int consentExpirationMin;

    @NotNull
    @Getter
    @Setter
    private String registrationUrl;
}

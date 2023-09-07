package com.yolt.providers.openbanking.ais.monzogroup.common;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Validated
@EqualsAndHashCode
public class MonzoGroupPropertiesV2 extends DefaultProperties {

    @NotNull
    @Getter
    @Setter
    private String registrationUrl;

    @NotNull
    @Getter
    @Setter
    private String registrationAudience;
}




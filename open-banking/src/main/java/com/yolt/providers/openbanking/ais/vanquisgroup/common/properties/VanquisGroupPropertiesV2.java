package com.yolt.providers.openbanking.ais.vanquisgroup.common.properties;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class VanquisGroupPropertiesV2 extends DefaultProperties {

    @NotNull
    private String registrationAudience;
    @NotNull
    private String registrationUrl;
}
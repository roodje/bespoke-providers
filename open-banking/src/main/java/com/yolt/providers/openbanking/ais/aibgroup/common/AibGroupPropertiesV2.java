package com.yolt.providers.openbanking.ais.aibgroup.common;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;

@Data
@Validated
public class AibGroupPropertiesV2 extends DefaultProperties {

    @NotNull
    private String registrationUrl;

    @NotNull
    private String registrationAudience;


    private String loginUrlAudience;
}

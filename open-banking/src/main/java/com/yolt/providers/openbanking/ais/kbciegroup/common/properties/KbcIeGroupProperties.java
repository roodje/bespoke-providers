package com.yolt.providers.openbanking.ais.kbciegroup.common.properties;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class KbcIeGroupProperties extends DefaultProperties {

    @NotNull
    private String registrationUrl;
}

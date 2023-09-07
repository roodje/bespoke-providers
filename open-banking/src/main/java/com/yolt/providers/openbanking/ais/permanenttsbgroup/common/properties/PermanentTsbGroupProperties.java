package com.yolt.providers.openbanking.ais.permanenttsbgroup.common.properties;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
public class PermanentTsbGroupProperties extends DefaultProperties {

    @NotNull
    private String registrationUrl;
}

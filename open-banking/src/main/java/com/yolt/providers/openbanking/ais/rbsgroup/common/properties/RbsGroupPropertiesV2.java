package com.yolt.providers.openbanking.ais.rbsgroup.common.properties;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RbsGroupPropertiesV2 extends DefaultProperties {

    @NotEmpty
    private String baseUrlPis;

    @NotEmpty
    private String audience;

    @NotEmpty
    private String registrationUrl;
}

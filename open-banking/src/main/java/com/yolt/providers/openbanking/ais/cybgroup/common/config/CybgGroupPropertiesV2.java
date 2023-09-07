package com.yolt.providers.openbanking.ais.cybgroup.common.config;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@RequiredArgsConstructor
public class CybgGroupPropertiesV2 extends DefaultProperties {

    @NotNull
    private String institutionId;

    @NotNull
    private String registrationUrl;
}

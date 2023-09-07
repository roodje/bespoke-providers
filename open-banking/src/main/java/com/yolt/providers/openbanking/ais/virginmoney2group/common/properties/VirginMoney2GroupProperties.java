package com.yolt.providers.openbanking.ais.virginmoney2group.common.properties;

import com.yolt.providers.openbanking.ais.generic2.DefaultProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;


@EqualsAndHashCode(callSuper = true)
@Data
public class VirginMoney2GroupProperties extends DefaultProperties {

    @NotNull
    private String registrationUrl;

    private String registrationAudience;
}
